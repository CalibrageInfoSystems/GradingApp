package com.oilpalm3f.gradingapp.dbmodels;

public class DataCountModel {

    private int Count;
    private String MethodName;

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        this.TableName = tableName;
    }

    public String TableName;

    public String getMethodName() {
        return MethodName;
    }

    public void setMethodName(String methodName) {
        this.MethodName = methodName;
    }

    public int getCount() {
        return Count;
    }

    public void setCount(int count) {
        this.Count = count;
    }

    public DataCountModel(int count, String MethodName, String tableName) {
        this.Count = count;
        this.MethodName = MethodName;
        this.TableName = tableName;
    }
}
