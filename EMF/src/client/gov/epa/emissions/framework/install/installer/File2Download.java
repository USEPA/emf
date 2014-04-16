/*
 * Created on Aug 11, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package gov.epa.emissions.framework.install.installer;

import java.lang.String;
import java.io.*;

/**
 * @author CEP User
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class File2Download implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String path;
	private String groups;
	private String date;
	private String version;
	private String param1;
	private String param2;
	private String param3;
	private String param4;
	private String param5;
	private String rest;
	private String size;
		
	public String getName(){
		return name;
	}
	
	public String getPath(){
		return path;
	}
	
	public String getGroups(){
		return groups;
	}
	
	public String getDate(){
		return date;
	}
	
	public String getVersion(){
		return version;
	}
	
	public String getParam1(){
		return param1;
	}
	
	public String getParam2(){
		return param2;
	}
	
	public String getParam3(){
		return param3;
	}
	
	public String getParam4(){
		return param4;
	}
	
	public String getParam5(){
		return param5;
	}
	
	public String getRest(){
		return rest;
	}
	
	public String getSize(){
		return size;
	}

	public void setName(String s){
		name = s;
	}
	
	public void setPath(String s){
		path = s;
	}
	
	public void setGroups(String s){
		groups = s;
	}
	
	public void setDate(String s){
		date = s;
	}
	
	public void setVersion(String s){
		version = s;
	}
	
	public void setParam1(String s){
		param1 = s;
	}
	
	public void setSize(String s){
		size = s;
	}
	public void setParam2(String s){
		param2 = s;
	}
	
	public void setParam3(String s){
		param3 = s;
	}
	
	public void setParam4(String s){
		param4 = s;
	}
	
	public void setParam5(String s){
		param5 = s;
	}
	
	public void setRest(String s){
		rest = s;
	}
}
