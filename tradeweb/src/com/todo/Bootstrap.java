package com.todo;

import spark.Request;
import spark.Response;
import spark.Route;
 
import static spark.Spark.*;

public class Bootstrap {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 get("/", new Route() {
	            @Override
	            public Object handle(Request request, Response response) {
	                return "Hello World!!";
	            }
	        });
	}

}
