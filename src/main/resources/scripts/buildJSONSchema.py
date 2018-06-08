#! /usr/bin/env python

import yaml
import sys
import re
import json
import urllib
import os

from jsonref import JsonRef
import click


class UnsupportedError(Exception):
	pass


def additional_properties(data):
	"This recreates the behaviour of kubectl at https://github.com/kubernetes/kubernetes/blob/225b9119d6a8f03fcbe3cc3d590c261965d928d0/pkg/kubectl/validation/schema.go#L312"
	new = {}
	try:
		for k, v in data.items():
			new_v = v
			if isinstance(v, dict):
				if "properties" in v:
					if "additionalProperties" not in v:
						v["additionalProperties"] = False
				new_v = additional_properties(v)
			else:
				new_v = v
			new[k] = new_v
		return new
	except AttributeError:
		return data


def replace_int_or_string(data):
	new = {}
	try:
		for k, v in data.items():
			new_v = v
			if isinstance(v, dict):
				if 'format' in v and v['format'] == 'int-or-string':
					new_v = {'oneOf': [
						{'type': 'string'},
						{'type': 'integer'},
					]}
				else:
					new_v = replace_int_or_string(v)
			elif isinstance(v, list):
				new_v = list()
				for x in v:
					new_v.append(replace_int_or_string(x))
			else:
				new_v = v
			new[k] = new_v
		return new
	except AttributeError:
		return data


def allow_null_optional_fields(data, parent=None, grand_parent=None, key=None):
	new = {}
	try:
		for k, v in data.items():
			new_v = v
			if isinstance(v, dict):
				new_v = allow_null_optional_fields(v, data, parent, k)
			elif isinstance(v, list):
				new_v = list()
				for x in v:
					new_v.append(allow_null_optional_fields(x, v, parent, k))
			elif isinstance(v, str):
				is_array = k == "type" and v == "array"
				is_string = k == "type" and v == "string"
				has_required_fields = grand_parent and "required" in grand_parent
				is_required_field = has_required_fields and key in grand_parent["required"]
				if is_array and not is_required_field:
					new_v = ["array", "null"]
				elif is_string and not is_required_field:
					new_v = ["string", "null"]
			new[k] = new_v
		return new
	except AttributeError:
		return data


def change_dict_values(d, prefix):
	new = {}
	try:
		for k, v in d.items():
			new_v = v
			if isinstance(v, dict):
				new_v = change_dict_values(v, prefix)
			elif isinstance(v, list):
				new_v = list()
				for x in v:
					new_v.append(change_dict_values(x, prefix))
			elif isinstance(v, str):
				if k == "$ref":
					new_v = "%s%s" % (prefix, v)
			else:
				new_v = v
			new[k] = new_v
		return new
	except AttributeError as e:
		return d

def apply_filters(specification, output, prefix= '', stand_alone=False, strict=False, removeMetaData=True):
	specification["$schema"] = "http://json-schema.org/draft-04/schema#"
	specification["type"] = "object"

	try:
		debug("Processing %s" % specification['title'])

		updated = change_dict_values(specification, prefix)
		specification = updated
		
		updated = allow_null_optional_fields(specification)
		specification = updated
		
		if stand_alone:
			base = "file://%s/" % (output)
			specification = JsonRef.replace_refs(specification, base_uri=base)

		if "additionalProperties" in specification:
			if specification["additionalProperties"]:
				updated = change_dict_values(specification["additionalProperties"], prefix)
				specification["additionalProperties"] = updated

		if strict and "properties" in specification:
			updated = additional_properties(specification["properties"])
			specification["properties"] = updated
			
			if removeMetaData and 'metadata' in specification['properties'] :
				specification['properties'].pop('metadata')

	except Exception as e:
		error("An error occured processing %s: %s" % (specification['title'], e))
	
	return specification


def info(message):
	click.echo(click.style(message, fg='green'))


def debug(message):
	click.echo(click.style(message, fg='yellow'))


def error(message):
	click.echo(click.style(message, fg='red'))


@click.command()
@click.option('-o', '--output', default='schemas', metavar='PATH', help='Directory to store schema files')
@click.option('-p', '--prefix', default='_definitions.json', help='Prefix for JSON references')
@click.option('--stand-alone', is_flag=True, help='Whether or not to de-reference JSON schemas')
@click.option('--strict', is_flag=True, help='Prohibits properties not in the schema (additionalProperties: false)')
@click.argument('schema', metavar='SCHEMA_URL')
def default(output, schema, prefix, stand_alone, strict):
	print(output)
	with open(schema, "r") as stream:
		try:
			data = yaml.load(stream)
			stream.close()
		except yaml.YAMLError as exc:
			print(exc)
	
	responseDefinitions = {}
	sharedDefinitions = data['definitions']
		
	for path in list(data['paths'].keys()):
		pathObj = data['paths'][path]
		for method in list(pathObj.keys()):
			if '200' in pathObj[method]['responses']:
				if 'schema' in pathObj[method]['responses']['200']:
					schemaObj = pathObj[method]['responses']['200']['schema']
					if 'title' not in schemaObj:
						schemaObj['title'] = method + path.replace('/', '').capitalize()
					tag = 'Untagged'
					if 'tags' in pathObj[method]:
						tag = pathObj[method]['tags'][0].replace(' ', '')
						
					if tag not in responseDefinitions:
						responseDefinitions[tag] = {}
					responseDefinitions[tag][schemaObj['title']] = schemaObj

	info("Generating individual response schemas")
	for tag in responseDefinitions:
		for title in responseDefinitions[tag]:
			specification = apply_filters(responseDefinitions[tag][title], output, '../definitions.json', stand_alone, strict, True)
			
			schema_file_path = "%s/%s" % (output, tag)
			if not os.path.exists(schema_file_path):
				os.makedirs(schema_file_path)
						
			schema_file_name = "%s/%s.json" % (schema_file_path, title.split('.')[-1].lower())
			with open(schema_file_name, 'w') as schema_file:
				debug("Generating %s" % schema_file_name)
				schema_file.write(json.dumps(specification, indent=2))

	info("Generating shared schemas")
	for title in sharedDefinitions:
		if 'title' not in sharedDefinitions[title]:
			sharedDefinitions[title]['title'] = title

		sharedDefinitions[title] = apply_filters(sharedDefinitions[title], output, '../definitions.json', stand_alone, strict, True)
	
	with open("%s/definitions.json" % output, 'w') as definitions_file:
		definitions_file.write(json.dumps({"definitions": sharedDefinitions}, indent=2))


if __name__ == '__main__':
	default()
