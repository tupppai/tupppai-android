package com.psgod.ui.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.psgod.PSGodApplication;

//获取省份的数组
public class CityInfo {
	public static String[] provinceNameArray = null;
	public static Integer[] provinceIdArray = null;
	public static String[][] cityNameArray = null;
	public static Integer[][] cityIdArray = null;

	private static void initCityData() {
		InputStream xml = null;
		try {
			xml = PSGodApplication.getAppContext().getAssets()
					.open("city/cities.xml");
		} catch (IOException e) {
			e.printStackTrace();
		}
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;

		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		org.w3c.dom.Document document = null;
		try {
			document = builder.parse(xml);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// provinces root节点
		Element element = document.getDocumentElement();

		NodeList provinces = element.getElementsByTagName("province");
		// 初始化
		provinceNameArray = new String[provinces.getLength()];
		provinceIdArray = new Integer[provinces.getLength()];
		cityNameArray = new String[provinces.getLength()][];
		cityIdArray = new Integer[provinces.getLength()][];

		for (int i = 0; i < provinces.getLength(); i++) {
			Element provinceElement = (Element) provinces.item(i);

			provinceNameArray[i] = provinceElement.getAttribute("name");
			provinceIdArray[i] = Integer.parseInt(provinceElement
					.getAttribute("tag"));

			NodeList childNodes = provinceElement.getChildNodes();

			ArrayList<String> cityNames = new ArrayList<String>();
			ArrayList<Integer> cityIds = new ArrayList<Integer>();

			for (int j = 0; j < childNodes.getLength(); j++) {
				if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
					Element cityElement = (Element) childNodes.item(j);
					if (cityElement.getNodeName().equals("city")) {
						cityNames.add(cityElement.getAttribute("name"));
						cityIds.add(Integer.valueOf(cityElement
								.getAttribute("tag")));
					}
				}
			}

			cityNameArray[i] = new String[cityNames.size()];
			cityIdArray[i] = new Integer[cityIds.size()];
			for (int n = 0; n < cityNames.size(); n++) {
				cityNameArray[i][n] = cityNames.get(n).toString();
				cityIdArray[i][n] = cityIds.get(n);
			}
		}
	}

	public static Integer[][] getCityId() {
		if (cityIdArray == null) {
			initCityData();
		}
		return cityIdArray;
	}

	public static String[][] getCityName() {
		if (cityNameArray == null) {
			initCityData();
		}
		return cityNameArray;
	}

	public static Integer[] getProvinceId() {
		if (provinceIdArray == null) {
			initCityData();
		}
		return provinceIdArray;
	}

	public static String[] getProvinceName() {
		if (provinceNameArray == null) {
			initCityData();
		}
		return provinceNameArray;
	}

	// 通过省份名字获得省份的id
	public static int getProvinceIdByName(String province) {
		if (provinceNameArray == null || provinceIdArray == null) {
			initCityData();
		}

		int provincePos = 0;
		for (int i = 0; i < provinceNameArray.length; i++) {
			if (provinceNameArray[i] == province) {
				provincePos = i;
			}
		}

		return provinceIdArray[provincePos];
	}

	// 通过省份id获得省份名字
	public static String getProvinceNameById(int id) {
		if (provinceNameArray == null || provinceIdArray == null) {
			initCityData();
		}
		int provincePos = 0;
		for (int i = 0; i < provinceIdArray.length; i++) {
			if (provinceIdArray[i] == id) {
				provincePos = i;
			}
		}
		return provinceNameArray[provincePos];
	}

	// 通过省份和城市的id 获得城市的名字
	public static String getCityNameById(int pid, int cid) {
		if (provinceNameArray == null || provinceIdArray == null
				|| cityNameArray == null || cityIdArray == null) {
			initCityData();
		}
		int provincePos = 0;
		int cityPos = 0;

		for (int i = 0; i < provinceIdArray.length; i++) {
			if (provinceIdArray[i] == pid) {
				provincePos = i;
			}
		}

		if (provincePos != 0) {
			for (int n = 0; n < cityIdArray[provincePos].length; n++) {
				if (cityIdArray[provincePos][n] == cid) {
					cityPos = n;
				}
			}
		}

		return cityNameArray[provincePos][cityPos];
	}

	// 通过省份和城市的名字 获得城市的id
	public static int getCityIdByName(String provinceName, String cityName) {
		if (provinceNameArray == null || provinceIdArray == null
				|| cityNameArray == null || cityIdArray == null) {
			initCityData();
		}
		int provincePos = 0;
		int cityPos = 0;

		for (int i = 0; i < provinceNameArray.length; i++) {
			if (provinceNameArray[i] == provinceName) {
				provincePos = i;
			}
		}

		if (provincePos != 0) {
			for (int n = 0; n < cityNameArray[provincePos].length; n++) {
				if (cityNameArray[provincePos][n] == cityName) {
					cityPos = n;
				}
			}
		}

		return cityIdArray[provincePos][cityPos];
	}
}