package com.beio.base.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.beio.base.entity.SysRegex;

/**
 * 正则工具类
 * @author zhs
 * @date 2017.04.03
 * @version 1.0.0
 */
public class RegexUtil extends SqlSessionDaoSupport{
	
	private static Map<String, SysRegex> regexs = new HashMap<String, SysRegex>();

	/**
	 * 初始化正则
	 */
	public void initRegexs() {
		synchronized (this) {
			if (ComUtil.isEmpty(regexs)) {
				regexs.clear();
				List<SysRegex> list = getSqlSession().selectList("sys.queryRegex");
				for (SysRegex sysRegex : list) {
					regexs.put(sysRegex.getName(), sysRegex);
				}
			}
		}
	}
	
	/**
	 * 初始化正则
	 */
	public void initRegexs(String key) {
		synchronized (this) {
			if (regexs.get(key) == null) {
				regexs.clear();
				List<SysRegex> list = getSqlSession().selectList("sys.queryRegex");
				for (SysRegex sysRegex : list) {
					regexs.put(sysRegex.getName(), sysRegex);
				}
			}
		}
	}

	public Map<String, SysRegex> getRegexs() {
		if (ComUtil.isEmpty(regexs)) {
			initRegexs();
		}
		return regexs;
	}

	public SysRegex getRegex(String key) {
		if (regexs.get(key) == null) {
			initRegexs(key);
		}
		return regexs.get(key);
	}
	
}
