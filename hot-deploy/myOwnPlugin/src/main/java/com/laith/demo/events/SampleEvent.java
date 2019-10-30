package com.laith.demo.events;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SampleEvent  {

	public static String handle(HttpServletRequest request, HttpServletResponse response) {
		List<Map<String, Object>> samples = new LinkedList<>();
		Map<String, Object> map = new HashMap<>();
		map.put("id", "Welcome");
		map.put("value", "A");
		map.put("key", "B");
		map.put("active", "B");
		samples.add(map);
		request.setAttribute("data", samples);
		return "success";
	}
}
