package gov.epa.mims.analysisengine.table;

public interface DoubleTable {
    void open() throws Exception;

    void close() throws Exception;

    int getColumnCount() throws Exception;

    String getColumnName(int var1) throws Exception;

    int getRowCount() throws Exception;

    boolean isValueAvailable(int var1, int var2) throws Exception;

    void waitForDataAvailable(int var1, int var2) throws Exception;

    double getDoubleAt(int var1, int var2) throws Exception;

    double[] getDoubles(int var1) throws Exception;

    boolean isCellEditable(int var1, int var2) throws Exception;

    String getRowName(int var1) throws Exception;

    void setRowName(int var1, String var2) throws Exception;

    int findRowByName(String var1) throws Exception;

    int findColumnByName(String var1) throws Exception;

    void setDoubleAt(int var1, int var2, double var3) throws Exception;

    void setDoubles(int var1, double[] var2) throws Exception;
}
