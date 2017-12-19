package com.mr.datainsert;

import java.sql.*;
public class Dataconn {
    public String env = "test";
	private String username;
	private String url;
	private String password;
	private Connection connection;
	public Dataconn()
	{
		String finalhost = "mysqldb";
		
		String finaluname = "SYSTEM";
		
	 
		String finaldb = "motradedb";
		String finalport = "3306";
		
	//finalhost = "localhost";
	//	finaluname = "root";
		
		String sqlhost = System.getenv("SQLHOST");
		String sqldb = System.getenv("SQLDB");
		String sqlport = System.getenv("SQLPORT");
		String sqluname = System.getenv("SQLUNAME");

if (sqlhost != null )
        	if(sqlhost.length() >=1)
        	{
        		System.out.println("read period from env variable as : "+sqlhost);
        		finalhost = sqlhost;
        	}
			
if (sqluname != null )
	if(sqluname.length() >=1)
	{
		System.out.println("read period from env variable as : "+sqluname);
		finaluname = sqluname;
	}
		
			if (sqldb != null )
        	if(sqldb.length() >=1)
        	{
        		System.out.println("read period from env variable as : "+sqldb);
        		finaldb = sqldb;
        	}
			
				if (sqlport != null )
        	if(sqlport.length() >=1)
        	{
        		System.out.println("read period from env variable as : "+sqlport);
        		finalport = sqlport;
        	}
			
		
	
			// local database
			this.url = "jdbc:mysql://"+finalhost+":"+finalport+"/"+finaldb;
			
			//	 this.username = "root"; //localsetting
			this.username=finaluname; //openshiftsetting
				 this.password = "jurong123";
				
		
	}
	public Connection getconn()
	{
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			this.connection = DriverManager.getConnection(this.url,this.username,this.password);
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.connection;
		
	}
	public void closeconn()
	{
		try {
			this.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
