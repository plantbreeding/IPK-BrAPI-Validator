package de.ipk_gatersleben.bit.bi.bridge.brapicomp.dbentities;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "stats")
public class Stat {
	
	public static final String TYPE_FIELD_NAME = "TYPE";
	
	public static final String DATE_FIELD_NAME = "DATE";

    @DatabaseField(columnName = TYPE_FIELD_NAME)
    private String type;
    
    @DatabaseField(columnName = DATE_FIELD_NAME)
    private Date date;

	public Stat() {
		super();
		setDate(new Date());
	}
	
	public Stat(String type) {
		super();
		setType(type);
		setDate(new Date());
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
