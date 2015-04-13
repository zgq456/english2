package sample.data.jpa.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JqGridData<T> {

	/** Total number of pages */
	private long total;
	/** The current page number */
	private long page;
	/** Total number of records */
	private long records;
	/** The actual data */
	private List<T> rows;

	public JqGridData(long total, long page, long records, List<T> rows) {
		this.total = total;
		this.page = page;
		this.records = records;
		this.rows = rows;
	}

	public long getTotal() {
		return this.total;
	}

	public long getPage() {
		return this.page;
	}

	public long getRecords() {
		return this.records;
	}

	public List<T> getRows() {
		return this.rows;
	}

	public String getJsonString() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("page", this.page);
		map.put("total", this.total);
		map.put("records", this.records);
		map.put("rows", this.rows);
		// return JSONValue.toJSONString(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonString = "";
		try {
			jsonString = mapper.writeValueAsString(map);
		}
		catch (JsonProcessingException e) {
			throw new UnsupportedOperationException("Auto-generated method stub", e);
		}
		return jsonString;
	}
}