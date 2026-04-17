package com.efile;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parsed E-language table, including metadata and row data.
 *
 * @author dingyh
 */
public class ETable {

	/**
	 * Table name.
	 */
	private String tableName;
	
	/**
	 * Table-level date metadata.
	 */
	private String date;
	/**
	 * Column names.
	 */
	private String columnNames[];
	/**
	 * Column types.
	 */
	private String types[];
	/**
	 * Column units.
	 */
	private String[] units;
	/**
	 * Column limit values.
	 */
	private String limitValues[];
	
	
	private List<Object[]> dataRows;
	
	/**
	 * Creates an empty parsed table model.
	 */
	public ETable() {
		dataRows = new ArrayList<Object[]>();
	}

	/**
	 * Returns the logical table name.
	 *
	 * @return parsed table name
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Sets the logical table name.
	 *
	 * @param tableName parsed table name
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Returns the parsed column names.
	 *
	 * @return column name array, or {@code null} when the table has no header yet
	 */
	public String[] getColumnNames() {
		return columnNames;
	}

	/**
	 * Sets the parsed column names.
	 *
	 * @param columnNames ordered column names
	 */
	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	/**
	 * Returns the parsed data rows.
	 *
	 * @return mutable list of parsed data rows
	 */
	public List<Object[]> getDataRows() {
		return dataRows;
	}

	/**
	 * Replaces the parsed data rows.
	 *
	 * @param dataRows parsed data rows to store
	 */
	public void setDataRows(List<Object[]> dataRows) {
		this.dataRows = dataRows;
	}

	/**
	 * Returns the parsed data rows.
	 *
	 * @return mutable list of parsed data rows
	 * @deprecated use {@link #getDataRows()} instead
	 */
	@Deprecated
	public List<Object[]> getDatas() {
		return getDataRows();
	}

	/**
	 * Replaces the parsed data rows.
	 *
	 * @param datas parsed data rows to store
	 * @deprecated use {@link #setDataRows(List)} instead
	 */
	@Deprecated
	public void setDatas(List<Object[]> datas) {
		setDataRows(datas);
	}

	/**
	 * Returns the table-level date metadata.
	 *
	 * @return date metadata, or an empty string when absent
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Sets the table-level date metadata.
	 *
	 * @param date date metadata captured from the source tag
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * Returns the parsed column types.
	 *
	 * @return column type array, or {@code null} when no type row is present
	 */
	public String[] getTypes() {
		return types;
	}

	/**
	 * Sets the parsed column types.
	 *
	 * @param types ordered column types
	 */
	public void setTypes(String[] types) {
		this.types = types;
	}

	/**
	 * Returns the parsed column units.
	 *
	 * @return unit array, or {@code null} when no unit row is present
	 */
	public String[] getUnits() {
		return units;
	}

	/**
	 * Sets the parsed column units.
	 *
	 * @param units ordered unit values
	 */
	public void setUnits(String[] units) {
		this.units = units;
	}

	/**
	 * Returns the parsed column units.
	 *
	 * @return unit array, or {@code null} when no unit row is present
	 * @deprecated use {@link #getUnits()} instead
	 */
	@Deprecated
	public String[] getUnites() {
		return getUnits();
	}

	/**
	 * Sets the parsed column units.
	 *
	 * @param unites ordered unit values
	 * @deprecated use {@link #setUnits(String[])} instead
	 */
	@Deprecated
	public void setUnites(String[] unites) {
		setUnits(unites);
	}

	/**
	 * Returns the parsed column limit values.
	 *
	 * @return limit value array, or {@code null} when no limit row is present
	 */
	public String[] getLimitValues() {
		return limitValues;
	}

	/**
	 * Sets the parsed column limit values.
	 *
	 * @param limitValues ordered limit values
	 */
	public void setLimitValues(String[] limitValues) {
		this.limitValues = limitValues;
	}
}
