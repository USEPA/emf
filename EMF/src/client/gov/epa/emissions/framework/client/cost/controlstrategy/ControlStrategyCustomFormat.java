package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.mims.analysisengine.UserPreferences;

import java.text.DecimalFormat;
import java.text.FieldPosition;

@SuppressWarnings("serial")
public class ControlStrategyCustomFormat extends DecimalFormat {

    public static final String NaN_FORMAT = "";

    /**
     * default empty constructor
     */
    public ControlStrategyCustomFormat() {
        setUserDefaults();
    }

    /**
     * default constructor with pattern
     * 
     * @pre pattern != null
     */
    public ControlStrategyCustomFormat(String pattern) {

        super(pattern);
        setUserDefaults();
    }

    private void setUserDefaults() {

        UserPreferences pref = UserPreferences.USER_PREFERENCES;
        String prefGrouping = pref.getProperty(UserPreferences.FORMAT_GROUPING);

        /*
         * grouping==true is the default
         */
        boolean grouping = true;
        try {
            if (prefGrouping != null && prefGrouping.trim().length() > 0) {
                grouping = Boolean.parseBoolean(prefGrouping);
            }
        } catch (Exception e) {
            /*
             * no-op
             */
        }

        this.setGroupingUsed(grouping);
    }

    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {

        StringBuffer retVal = null;
        if (Double.isNaN(number)) {
            retVal = new StringBuffer(NaN_FORMAT);
        } else {
            retVal = super.format(number, result, fieldPosition);
        }
        
        return retVal;
    }
}
