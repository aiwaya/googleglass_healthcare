package com.aki.utils;

import java.util.Map;

public interface OnSalesforceTaskCompleted {

	public void successSalesforceTask(int code, Map<String, Object> result);

	public void errorSalesforceTask(int code, MySalesforceException e);

}
