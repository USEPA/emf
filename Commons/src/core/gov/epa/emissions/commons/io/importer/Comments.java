package gov.epa.emissions.commons.io.importer;

import java.util.Iterator;
import java.util.List;

public class Comments {

    private List comments;

    public Comments(List comments) {
        this.comments = comments;
    }

    /**
     * Do not specify '#'. Implicit.
     */
    public String content(String tag) {
        return content("#", tag);
    }
    
    public String content(String symbol, String tag) {
        tag = symbol + tag;
        
        for (Iterator iter = comments.iterator(); iter.hasNext();) {
            String comment = (String) iter.next();
            if (comment != null && comment.toLowerCase().startsWith(tag.toLowerCase())) {
                return comment.substring(tag.length()).trim();
            }
        }

        return null;
    }

    /**
     * Have a comment starting with
     *  'tag'. tag -Do not specify '#'. Implicit.
     */
    public boolean have(String tag) {
        return content(tag) != null;
    }

    public String all() {
        StringBuffer description = new StringBuffer();
        for (Iterator iter = comments.iterator(); iter.hasNext();)
            description.append(iter.next() + "\n");

        return description.toString();
    }

    /**
     * Do not specify '#'. Implicit.
     */
    public boolean hasContent(String tag) {
        return hasContent("#", tag);
    }
    
    /**
     * Exmplicit tag.
     */
    public boolean hasContent(String symbol, String tag) {
        String comment = content(symbol, tag);
        return comment != null && comment.length() > 0;
    }
    
    /**
     * SMOKE doesn't like things like #ORLPOINT.
     * The right format should be '#ORL' or '#ORL POINT', etc.
     * 
     */

    public boolean hasRightTagFormat(String symbol, String tag) {
        tag = symbol + tag;
        String comment = null;
        
        for (Iterator<?> iter = comments.iterator(); iter.hasNext();) {
            String temp = (String) iter.next();
            
            if (temp != null && temp.trim().toLowerCase().startsWith(tag.toLowerCase())) 
                comment = temp.trim().substring(tag.length());
        }
        
        if (comment == null)
            return false;
        
        if (comment.length() > 0 && comment.charAt(0) != ' ')
            return false;
        
        return true;
    }
    
    public boolean hasRightTagFormat(String tag) {
        return hasRightTagFormat("#", tag);
    }
}
