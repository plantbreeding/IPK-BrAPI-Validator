# BrAPI Validator (BRAVA)

The BrAPI Validator (BRAVA) runs validation tests for BrAPI compliance.

Here you find the following modules for the application

* [api](api/README.md) - The spring API, exposes the validation software in the code module via rest services
* buildSrc - A gradle plugin for controlling the build process of the modules
* [cli](cli/README.md) - The command line version that makes use of the core module
* [core](core/README.md) - The core validation code and util classes
* [data](data/README.md) - Interfaces for spring data support
* [jpa](jpa/README.md) - Adds support for persisting validation reports in a SQL database using Spring JPA
* [mongodb](mongodb/README.md) - Adds support for persisting validation reports in a mongodb database using Spring
* gradle - The gradle wrapper for building, installing the modules
* [web](web/README.md) - A simple web application (an alternative to the Angular BRAVA app)
  that makes use of the spring API using thymeleaf templates
