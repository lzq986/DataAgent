/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.dataagent.util;

import com.alibaba.cloud.ai.dataagent.constant.DocumentMetadataConstant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchUtil {

	/**
	 * 构建过滤表达式字符串，目前FilterExpressionBuilder 不支持链式拼接元数据过滤，所以只能使用字符串拼接
	 * @param filterMap
	 * @return
	 */
	public static String buildFilterExpressionString(Map<String, Object> filterMap) {
		if (filterMap == null || filterMap.isEmpty()) {
			return null;
		}

		// 验证键名是否合法（只包含字母、数字和下划线）
		for (String key : filterMap.keySet()) {
			if (!key.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
				throw new IllegalArgumentException("Invalid key name: " + key
						+ ". Keys must start with a letter or underscore and contain only alphanumeric characters and underscores.");
			}
		}

		return filterMap.entrySet().stream().map(entry -> {
			String key = entry.getKey();
			Object value = entry.getValue();

			// 处理空值
			if (value == null) {
				return key + " == null";
			}

			// 根据值的类型决定如何格式化
			if (value instanceof String) {
				// 转义字符串中的特殊字符
				String escapedValue = escapeStringLiteral((String) value);
				return key + " == '" + escapedValue + "'";
			}
			else if (value instanceof Number) {
				// 数字类型直接使用
				return key + " == " + value;
			}
			else if (value instanceof Boolean) {
				// 布尔值使用小写形式
				return key + " == " + ((Boolean) value).toString().toLowerCase();
			}
			else if (value instanceof Enum) {
				// 枚举类型，转换为字符串并转义
				String enumValue = ((Enum<?>) value).name();
				String escapedValue = escapeStringLiteral(enumValue);
				return key + " == '" + escapedValue + "'";
			}
			else {
				// 其他类型尝试转换为字符串并转义
				String stringValue = value.toString();
				String escapedValue = escapeStringLiteral(stringValue);
				return key + " == '" + escapedValue + "'";
			}
		}).collect(Collectors.joining(" && "));
	}

	/**
	 * 转义字符串字面量中的特殊字符
	 */
	public static String escapeStringLiteral(String input) {
		if (input == null) {
			return "";
		}

		// 转义反斜杠和单引号
		String escaped = input.replace("\\", "\\\\").replace("'", "\\'");

		// 转义其他特殊字符
		escaped = escaped.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t")
			.replace("\b", "\\b")
			.replace("\f", "\\f");

		return escaped;
	}

	/**
	 * 构造元数据过滤表达式
	 * @param agentId 代理ID
	 * @param tableNames 表名列表
	 * @return SQL风格的过滤表达式字符串
	 */
	public static String buildFilterExpressionForSearchTables(String agentId, List<String> tableNames) {
		// 输出:agentId == 'agent123' && vectorType == 'text' && name in ['users', 'orders',
		// 'products']
		StringBuilder expression = new StringBuilder();
		expression.append("agentId == '").append(escapeStringLiteral(agentId)).append("'");
		expression.append(" && vectorType == '")
			.append(escapeStringLiteral(DocumentMetadataConstant.TABLE))
			.append("'");

		// 添加tableNames的IN条件（如果列表不为空）
		if (tableNames != null && !tableNames.isEmpty()) {
			String tableNamesList = tableNames.stream()
				.map(SearchUtil::escapeStringLiteral)
				.collect(Collectors.joining("', '", "'", "'"));

			expression.append(" && name in [").append(tableNamesList).append("]");
		}

		return expression.toString();
	}

	public static String buildFilterExpressionForSearchColumns(String agentId, String upstreamTableName,
			List<String> columnNames) {
		// agentId == 'agent123' && tableName == 'users' && vectorType == 'column' && name
		// in ['id', 'name', 'email']
		StringBuilder expression = new StringBuilder();

		// 添加 agentId 条件
		expression.append("agentId == '").append(escapeStringLiteral(agentId)).append("'");

		// 添加 tableName 条件
		expression.append(" && tableName == '").append(escapeStringLiteral(upstreamTableName)).append("'");

		// 添加 vectorType 条件（固定为 column）
		expression.append(" && vectorType == '")
			.append(escapeStringLiteral(DocumentMetadataConstant.COLUMN))
			.append("'");

		// 添加 columnNames 的 IN 条件（如果列表不为空）
		if (columnNames != null && !columnNames.isEmpty()) {
			String columnNamesList = columnNames.stream()
				.map(SearchUtil::escapeStringLiteral)
				.collect(Collectors.joining("', '", "'", "'"));

			expression.append(" && name in [").append(columnNamesList).append("]");
		}

		return expression.toString();
	}

	private SearchUtil() {
		throw new AssertionError("Cannot instantiate utility class");
	}

}
