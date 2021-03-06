package com.mr.newapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import com.mr.newdata.StockOtherTechnicals;
import com.mr.newdata.TickData;
import com.mr.datainsert.Dataconn;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
public class Nsebasedatadnldcarmilla implements java.lang.Runnable{

    public String trensetting = null;
	public int interval = 0;
	public int dayrange = 0;
	public String actualinterval;
	public int stochperiod = 0;
	public String usemonth="N";
	public Nsebasedatadnldcarmilla(String p) {
		// TODO Auto-generated constructor stub
		this.interval = Integer.parseInt(p);
		
		this.actualinterval = p;
		this.dayrange = this.interval/5;
		
		if(this.interval == 5)
			this.dayrange=2;
		
		if(this.interval == 60)
			this.dayrange=20;
		
		if(this.interval == 300)
			this.dayrange=90;
		
		this.interval = this.interval*60;
	}
	
	public Nsebasedatadnldcarmilla(String duration, String tndsetting,String stochperiod, String usemonth) {
		// TODO Auto-generated constructor stub
		System.out.println("using"+tndsetting);
		this.interval = Integer.parseInt(duration);
		this.stochperiod = Integer.parseInt(stochperiod);
		this.actualinterval = duration;
		this.dayrange = this.interval/5;
		this.trensetting = tndsetting;
		this.usemonth =usemonth;
		if(this.interval == 5)
			this.dayrange=2;
		
		if(this.interval == 60)
			this.dayrange=20;
		
		if(this.interval == 300)
			this.dayrange=90;
		
		this.interval = this.interval*60;
	}
	


	public StockOtherTechnicals getstochdata(String stocksymbol, Connection conn) throws MalformedURLException, ParseException
	{
		String strt = null;
        SimpleDateFormat sf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");  
        SimpleDateFormat sf1 = new SimpleDateFormat("dd MMM yyyy HH:mm:ss"); 
        
        //currentdate in SGT.
       
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
         int monthtoload = year*100+month-1;
         
         int weektoload = year*100+c.get(Calendar.WEEK_OF_YEAR)-1;
         
       
      //  System.out.println(year*100+month);
         float monthopen =0;
         float monthclose = 0;
         float monthlow = 0;
         float monthhigh = 0;
         
         
         float weekopen = 0;
         float weekclose = 0;
         float weeklow = 0;
         float weekhigh = 0;
         
         
		//System.out.println(stocksymbol+"entering intraday");
		String Stocksymbol = stocksymbol;
		double indick=0,indicd=0,wpr=0,rsi=0;
		Float openprice,closeprice = 0f,highprice,lowprice;
		Float fopen=0f,fclose=0f,fhigh=0f,flow=0f;
		double sma50 =0;
		Float C_openprice = 0f,C_closeprice =0f,C_highprice =0f,C_lowprice =0f;
		Float obvvolume = 0f ,currentvolume,C_currentvolume;
		Long timestamp = null;
		Date tickstart,tickend;
		URL tdlink = new URL("https://finance.google.com/finance/getprices?q="+stocksymbol+"&x=NSE&i="+this.interval+"&p=250d&f=d,o,h,l,c,v");
		if(this.interval/60 == 300)
			tdlink = new URL("https://finance.google.com/finance/getprices?q="+stocksymbol+"&x=NSE&p=5Y&f=d,o,h,l,c,v");
		if(this.interval/60 == 600)
			tdlink = new URL("https://finance.google.com/finance/getprices?q="+stocksymbol+"&x=NSE&p=5Y&f=d,o,h,l,c,v");
		if(this.interval/60 == 900)
			tdlink = new URL("https://finance.google.com/finance/getprices?q="+stocksymbol+"&x=NSE&p=15Y&f=d,o,h,l,c,v");
		
		URLConnection conn1 =null;
		//System.out.println(tdlink);
        ArrayList<Float> low = new ArrayList<Float>();
        ArrayList<Float> high = new ArrayList<Float>();
        ArrayList<Float> obvvol = new ArrayList<Float>();
        ArrayList<Float> close = new ArrayList<Float>();
        ArrayList<Float> highlow2 = new ArrayList<Float>();
        
        ArrayList<Float> monthlowarray = new ArrayList<Float>();
        ArrayList<Float> monthhigharray = new ArrayList<Float>();
        ArrayList<Float> monthclosearray = new ArrayList<Float>();
        
        
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//	System.out.println("lastrun: " + new Date());
		try {
			conn1 = tdlink.openConnection();
		
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return new StockOtherTechnicals();
		}
		//System.out.println("Processing :" + string);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			return new StockOtherTechnicals();
		}
		
		
		ArrayList<TickData> tickhash = new ArrayList<TickData>();
		String inputLine;
		boolean start =false;
        try {
			while ((inputLine = in.readLine()) != null)
				{
		//System.out.println(inputLine);
				if (!start && inputLine.startsWith("a"))
				{
                    start=true;
					StringTokenizer st = new StringTokenizer(inputLine,",");
					String timestampstring = (String) st.nextElement();
					timestamp = Long.valueOf(timestampstring.substring(1));
					
					Calendar tickdate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
					tickdate.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
					tickdate.setTimeInMillis((Long)timestamp*1000L) ;
					closeprice=Float.valueOf((String) st.nextElement());
					highprice=Float.valueOf((String) st.nextElement());
					lowprice=Float.valueOf((String) st.nextElement());
					openprice=Float.valueOf((String) st.nextElement());
					currentvolume= Float.valueOf((String) st.nextElement());
					
					sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
					strt = sf.format(tickdate.getTime());
					//
					//tickend = tickdate.getTime();
					strt = strt.replaceAll("SGT", "");
					//
					//tickend = tickdate.getTime();
					tickend = sf1.parse(strt);
				//	System.out.println(tickend);
					Calendar tickstartdate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
					tickstartdate.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
					//if(!this.actualinterval.equals("300") && !this.actualinterval.equals("600") )
					if(Integer.parseInt(this.actualinterval) < 250)
						tickstartdate.setTimeInMillis((Long)(timestamp-300)*1000L) ;
					else
						tickstartdate.setTimeInMillis((Long)(timestamp)*1000L) ;
					//tickstart = tickstartdate.getTime();
					
					sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
					strt = sf.format(tickstartdate.getTime());
					//
					//tickend = tickdate.getTime();
					strt = strt.replaceAll("SGT", "");
					//
					//tickend = tickdate.getTime();
					tickstart = sf1.parse(strt);
					
					TickData  tickd = new TickData();
					tickd.setOpenprice(openprice);
					tickd.setHighprice(highprice);
					tickd.setLowprice(lowprice);
					tickd.setCloseprice(closeprice);
					tickd.setTickstart(tickstart);
					tickd.setTickend(tickend);
					tickd.setVolume(currentvolume);
					
					tickhash.add(tickd);
					
					
					continue;
					//System.out.println(obvvolume+"::"+currentvolume);
				}
				else if (start )
				{
					
					if (!inputLine.isEmpty())
					{
						Long currenttimestamp = 0L;
						StringTokenizer st = new StringTokenizer(inputLine,",");
						String timevar = (String)st.nextElement();
						if(timevar.startsWith("a"))
						{
							timevar = timevar.substring(1);
							timestamp = Long.valueOf(timevar);
							currenttimestamp = timestamp;
						}
						else
						{
						Long rg = Long.valueOf(timevar);
						//if(!this.actualinterval.equals("300") && !this.actualinterval.equals("600"))
						if(Integer.parseInt(this.actualinterval) < 250)
						currenttimestamp = timestamp + (rg*300);
						else
							currenttimestamp = timestamp + (rg*86400);
						}
						Calendar tickdate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
						tickdate.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
						tickdate.setTimeInMillis((Long)currenttimestamp*1000L) ;
						
						
						sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
						strt = sf.format(tickdate.getTime());
						//System.out.println(strt);
						strt = strt.replaceAll("SGT", "");
						//
						//tickend = tickdate.getTime();
						tickend = sf1.parse(strt);
				//	System.out.println(tickend);
						
						C_closeprice=Float.valueOf((String) st.nextElement());
						C_highprice=Float.valueOf((String) st.nextElement());
						C_lowprice=Float.valueOf((String) st.nextElement());
						C_openprice=Float.valueOf((String) st.nextElement());
						C_currentvolume= Float.valueOf((String) st.nextElement());
						
						Calendar tickstartdate = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
						tickstartdate.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
						//if(!this.actualinterval.equals("300") && !this.actualinterval.equals("600"))
						if(Integer.parseInt(this.actualinterval) < 250)
							tickstartdate.setTimeInMillis((Long)(currenttimestamp-300)*1000L) ;
							else
								tickstartdate.setTimeInMillis((Long)(currenttimestamp)*1000L) ;
						
	
						
						sf.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
						strt = sf.format(tickstartdate.getTime());
						//
						//tickend = tickdate.getTime();
						strt = strt.replaceAll("SGT", "");
						//
						//tickend = tickdate.getTime();
						tickstart = sf1.parse(strt);
						
						
						
						
						TickData  tickd = new TickData();
						tickd.setOpenprice(C_openprice);
						tickd.setHighprice(C_highprice);
						tickd.setLowprice(C_lowprice);
						tickd.setCloseprice(C_closeprice);
						tickd.setTickstart(tickstart);
						tickd.setTickend(tickend);
						tickd.setVolume(C_currentvolume);
						
						tickhash.add(tickd);
						
						

						
						
						
						
					}
				
				
				
				}
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        ArrayList<TickData> Converttick = new ArrayList<TickData>();
        ArrayList<TickData> monthtick = new ArrayList<TickData>();
       if((this.interval/60) > 400 && (this.interval/60) < 800 )
    	   Converttick = getweektick(tickhash,this.interval);
       else if((this.interval/60) > 800 && (this.interval/60) < 1100 )
    	   Converttick = getmonthtick(tickhash,this.interval);
       else
        Converttick = tickhash;
        // check this logic
        
       if(this.usemonth.equals("Y"))
       {
    	   System.out.println("using month");
    	   if((this.interval/60) < 850)
              monthtick = getmonthtick(tickhash,this.interval);
              else
            	  monthtick =Converttick;
    	    
       }
       else
       {
    	   monthtick =Converttick;
       }
        /* create and add to the data
         * 
         *
        low.add(lowprice);
		high.add(highprice);
		obvvol.add(obvvolume);
		close.add(closeprice);
        
		obvvolume=getobvvol(C_highprice,C_lowprice,C_closeprice,C_currentvolume,obvvolume,closeprice);
		//System.out.println(obvvolume+"::"+C_currentvolume+":;"+C_closeprice+":;"+closeprice);
		closeprice = C_closeprice;
		low.add(C_lowprice);
		high.add(C_highprice);
		obvvol.add(obvvolume);
		
        */
        
        for (int ik =0;ik < Converttick.size();ik++)
        {
        	float prevclose = 0f;
        	TickData Currtickdata = Converttick.get(ik);
        	if (ik > 0)
        	{
        		TickData prevtickdata = Converttick.get(ik-1);
        		prevclose = prevtickdata.getCloseprice();
        		obvvolume=getobvvol(Currtickdata.getHighprice(),Currtickdata.getLowprice(),Currtickdata.getCloseprice(),Currtickdata.getVolume(),obvvolume,prevclose);
        	}
        	else
        	{
        		prevclose = Currtickdata.getCloseprice();
        		obvvolume = Currtickdata.getVolume();
        	}
        	
        	fopen= Currtickdata.getOpenprice();
        	fhigh= Currtickdata.getHighprice();
        	flow=Currtickdata.getLowprice();
        	fclose=Currtickdata.getCloseprice();
        	
        	low.add(Currtickdata.getLowprice());
        	high.add(Currtickdata.getHighprice());
        	close.add(Currtickdata.getCloseprice());
        	
        	obvvol.add(obvvolume);
        	
        	if(Integer.parseInt(this.actualinterval) == 300)
			{
        		Calendar cal1 = Calendar.getInstance();
        		 cal1.setTime(Currtickdata.getTickend());
        		// System.out.println(Currtickdata.getTickend()+""+cal1.get(Calendar.WEEK_OF_YEAR));
				if((Currtickdata.getTickend().getYear()+1900)*100+Currtickdata.getTickend().getMonth() == monthtoload )
				{
					monthclose = fclose;
					if(monthopen == 0)
					{
						monthopen = fopen;
						
						monthlow = flow;
						monthhigh = fhigh;
					}
					else
					{
						if(flow < monthlow)
							monthlow = flow;
						if(fhigh > monthhigh)
							monthhigh = fhigh;
						
					}
				}
				
				if((Currtickdata.getTickend().getYear()+1900)*100+cal1.get(Calendar.WEEK_OF_YEAR) == weektoload )
				{
					weekclose = fclose;
					if(weekopen == 0)
					{
						weekopen = fopen;
						
						weeklow = flow;
						weekhigh = fhigh;
					}
					else
					{
						if(flow < weeklow)
							weeklow = flow;
						if(fhigh > weekhigh)
							weekhigh = fhigh;
						
					}
				}
				
			}
        	
        }
        
        for (int il=0; il <monthtick.size();il++)
        {
        	TickData Currtickdata = monthtick.get(il);
        	
        	monthlowarray.add(Currtickdata.getLowprice());
        	monthhigharray.add(Currtickdata.getHighprice());
        	monthclosearray.add(Currtickdata.getCloseprice());
        	
        }
        
        float[] mlarray = new float[monthlowarray.size()];
        int oo =0;
        
        for (Float f : monthlowarray)
        {
        	mlarray[oo++] = (f !=null ? f: Float.NaN);
        }
        
        oo=0;
        float[] mharray = new float[monthhigharray.size()];
        
        for (Float f : monthhigharray)
        {
        	mharray[oo++] = (f !=null ? f: Float.NaN);
        }
        
        
        oo=0;
        
        float[] mcarray = new float[monthclosearray.size()];
        
        for (Float f: monthclosearray)
        {
        	mcarray[oo++] = (f !=null ? f: Float.NaN);
        }
        
        float[] lowArray = new float[low.size()];
        int k = 0;

        for (Float f : low) {
        	lowArray[k++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        
        float[] highArray = new float[high.size()];
        k = 0;

        for (Float f : high) {
        	highArray[k++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        
        float[] closeArray = new float[close.size()];
        k = 0;

        for (Float f : close) {
        	closeArray[k++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        
        
        float[] obvArray = new float[obvvol.size()];
       k = 0;

        for (Float f : obvvol) {
        	obvArray[k++] = f; // Or whatever default you want.
        }
        
        
        // start computation here.
        Float lowdiff;
        Float low_lowdiff =1000000000f;
        Float high_lowdiff=0f;
        
        Float highdiff;
        Float low_highdiff =1000000000f;
        Float high_highdiff=0f;
        
        Integer Length = low.size();
        Core c1 = new Core();
        double[] closePrice = new double[7000];
        double[] out = new double[7000];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        
        ArrayList<Float> lowdiffarr = new ArrayList<Float>();
        ArrayList<Float> highdiffarr = new ArrayList<Float>();
        
        	
        		 RetCode retCode =c1.wma(0, lowArray.length-1, lowArray, 50, begin, length, out) ;
        		 if (retCode == RetCode.Success) {
        			 for (int i = begin.value; i < length.value+begin.value; i++) {
        				 lowdiff = lowArray[i] - Float.valueOf(String.valueOf(out[i-begin.value]));
            			// System.out.println(i+":"+lowdiff+";"+lowArray[i]+"::"+out[i-begin.value]);
            			 lowdiffarr.add(lowdiff);
        			 }
        		 }
        			
        			 
        		 retCode =c1.wma(0, highArray.length-1, highArray, 50, begin, length, out) ;
        		 if (retCode == RetCode.Success) {
        			 for (int i = begin.value; i < length.value+begin.value; i++) {
        				 highdiff = highArray[i] - Float.valueOf(String.valueOf(out[i-begin.value]));
            			 
            			 highdiffarr.add(highdiff);
        			 }
        		 }
        			
        		 
        		
        		
        		 
        	
        	
        	float[] highdiffArray = new float[highdiffarr.size()];
            k = 0;

            for (Float f : highdiffarr) {
            	highdiffArray[k++] = (f != null ? f : Float.NaN); // Or whatever default you want.
            } 
            
        	float[] lowdiffArray = new float[lowdiffarr.size()];
            k = 0;

            for (Float f : lowdiffarr) {
            	lowdiffArray[k++] = (f != null ? f : Float.NaN); // Or whatever default you want.
            } 
            
            
          
            
        // lowdiffArray, highdiffarray, obvArray,lowarray ,closeArrayready
        	
            DecimalFormat df1 = new DecimalFormat("#.##"); 
        
          
         double [] vals = getstochvals(lowdiffArray,highdiffArray,obvArray,lowArray,retCode,c1);
         //   double [] vals = getrsistochvals(closeArray,retCode,c1,this.stochperiod);
          indick = Double.valueOf(df1.format(vals[0]));
          indicd=Double.valueOf(df1.format(vals[1]));
		 
           retCode = c1.willR(0, closeArray.length-1, highArray, lowArray, closeArray, 21, begin, length, out);
           if (retCode == RetCode.Success) {
        	   try{
        	   wpr= Double.valueOf(df1.format(out[length.value-1]));
        	   }
        	   catch(Exception e)
        	   {
        		   wpr=75;
        	   }
           }
		 
           retCode = c1.rsi(0, closeArray.length-1, closeArray, 14, begin, length, out);
  if (retCode == RetCode.Success) {
	  try{
	  rsi= Double.valueOf(df1.format(out[length.value-1]));
	  }
	  catch(Exception e)
	  {
		  rsi=75;
	  }
        	   
           }
  
  retCode = c1.sma(0, closeArray.length-1, closeArray, 50, begin, length, out);

  if (retCode == RetCode.Success) {
	  try{
	  sma50= Double.valueOf(df1.format(out[length.value-1]));
	  }
	  catch(Exception e)
	  {
		  
		  sma50=C_closeprice;
	  }
        	   
           }
 
 float [] sptnd = getsupertrend(mcarray,mharray,mlarray,c1,this.actualinterval,this.trensetting);
 float [] sptnd7 = getsupertrend7(closeArray,highArray,lowArray,c1);
 // float [] linragg = getLinearggslope(closeArray,highArray,lowArray,c1,100);
 // System.out.println("linearreg:"+Stocksymbol +":"+ linragg[0]);
 // System.out.println("linearregup:"+Stocksymbol +":"+ (linragg[0]+(2*linragg[1])));
 // System.out.println("linearregdwn:"+Stocksymbol +":"+ (linragg[0]-(2*linragg[1])));
  StockOtherTechnicals st = new StockOtherTechnicals();
  st.setStocksymbol(Stocksymbol);
  st.setDayk(indick);
  st.setDayd(indicd);
  st.setDaywilliamsr(wpr);
  st.setDayrsi(rsi);
  st.setOpen(fopen);
  st.setHigh(fhigh);
  st.setLow(flow);
  st.setClose(fclose);
  st.setSma50(sma50);
  st.setDownvalue(sptnd[1]);
  st.setUpvalue(sptnd[0]);
  st.setDownvalue7(sptnd7[1]);
  st.setUpvalue7(sptnd7[0]);
  if(sptnd[2] == -1)
  st.setTrend("Down");
  else if (sptnd[2] == 1)
	  st.setTrend("Up");
  else
	  st.setTrend("NA");
System.out.println(st.getStocksymbol()+":"+st.getDownvalue()+":"+st.getUpvalue()+":"+st.getDayk()+":"+st.getDayd());
  
  //System.out.println(Stocksymbol+"::"+monthopen+"::"+monthhigh+"::"+monthlow+"::"+monthclose);
  if(monthopen!= 0)
	  updatemonthlycamarilla(conn,monthopen,monthhigh,monthlow,monthclose,stocksymbol);
  if(weekopen!=0)
	  updateweeklycamarilla(conn,weekopen,weekhigh,weeklow,weekclose,stocksymbol);
  
  
  
  //System.out.println(Stocksymbol+"::"+weekopen+"::"+weekhigh+"::"+weeklow+"::"+weekclose);
  
          return st;
	}   
	

	private double[] getrsistochvals(float[] closeArray, RetCode retCode, Core c1,int pd) {
		// TODO Auto-generated method stub
		 double[] outFastK = new double[7000];
		 double[] outFastD = new double[7000];
	        MInteger begin = new MInteger();
	        MInteger length = new MInteger();
        float stk = 50,std = 50;
        
        retCode = c1.stochRsi(0, closeArray.length-1, closeArray, pd, 3, 3, MAType.Sma, begin, length, outFastK, outFastD);
        if (retCode == RetCode.Success) {
     	   try{
     	   stk = (float) outFastK[length.value-1];
     	   System.out.println(outFastK[length.value-1]);
     	   std = (float) outFastD[length.value-1];
     	  System.out.println(outFastD[length.value-1]);
     	   }
     	   catch(Exception e)
     	   {
     		  stk=50;
     		  std=50;
     	   }
     	   
     	   return new double[] {stk,std};
        }
        
        return new double[] {stk,std};
	}

	private ArrayList<TickData> getmonthtick(ArrayList<TickData> tickhash, int interval2) {
		// TODO Auto-generated method stub

		// TODO Auto-generated method stub
		Calendar cal1 = Calendar.getInstance();
		ArrayList <TickData> result = new ArrayList<TickData>();        		
		int currentweek= 0;
		float openprice = 0,closeprice = 0,lowprice = 0,highprice = 0,volume =0f;	
		Date tickstarttime = null,tickendtime;
						
						  for(int i=0 ; i< tickhash.size();i++) 
						{
						
						  TickData currdata = tickhash.get(i);
						   cal1.setTime(currdata.getTickend());
						    if(i==0)
							{
							 currentweek = cal1.get(Calendar.MONTH) ;
							 openprice = currdata.getOpenprice();
							 closeprice = currdata.getCloseprice();
							 lowprice = currdata.getLowprice();
							 highprice = currdata.getHighprice();
							 volume = currdata.getVolume();
							 
							}
							else if (i == tickhash.size()-1)
							{
							TickData newdata = new TickData();
								 newdata.setOpenprice(openprice);
			        	  newdata.setCloseprice(closeprice);
			        	  newdata.setHighprice(highprice);
			        	  newdata.setLowprice(lowprice);
			        	  //newdata.setTickstart(tickstarttime);
			        	 // newdata.setTickend(tickendtime);
			        	  newdata.setVolume(volume);
						  result.add(newdata);
						  System.out.println("lasttick");
						  System.out.println("open:"+newdata.getOpenprice()+" High: "+newdata.getHighprice()+" low: "+newdata.getLowprice() +" Close: "+newdata.getCloseprice());
							}
							else
							{
							  if (currentweek == cal1.get(Calendar.MONTH))
							  {
							     if (currdata.getLowprice() < lowprice)
								   lowprice = currdata.getLowprice();
								 if (currdata.getHighprice() > highprice)
								 highprice = currdata.getHighprice();
								 closeprice = currdata.getCloseprice();
								 volume = volume + currdata.getVolume();
							  }
							  else
							  {
							     TickData newdata = new TickData();
								 newdata.setOpenprice(openprice);
			        	  newdata.setCloseprice(closeprice);
			        	  newdata.setHighprice(highprice);
			        	  newdata.setLowprice(lowprice);
			        	  //newdata.setTickstart(tickstarttime);
			        	 // newdata.setTickend(tickendtime);
			        	  newdata.setVolume(volume);
			        	  
			        	  System.out.println("open:"+newdata.getOpenprice()+" High: "+newdata.getHighprice()+" low: "+newdata.getLowprice() +" Close: "+newdata.getCloseprice());
						  result.add(newdata);
						  
						  currentweek = cal1.get(Calendar.MONTH) ;
							 openprice = currdata.getOpenprice();
							 closeprice = currdata.getCloseprice();
							 lowprice = currdata.getLowprice();
							 highprice = currdata.getLowprice();
							 volume = currdata.getVolume();
						  
						  
						  
							  }
							  
							}
						
						
						}
						  return result;
	
	}

	private float[] getLinearggslope(float[] closeArray, float[] highArray, float[] lowArray, Core c1,int pd) {
		// TODO Auto-generated method stub
		double[] out = new double[7000];
		
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        float val = 0f;
        float dev = 0f;
        if (closeArray.length > pd+10)
        {
        RetCode retCode =c1.linearReg(0, closeArray.length-1, closeArray, pd, begin, length, out);
  		 if (retCode == RetCode.Success) {
  			val= (float) out[length.value-1];
  		 }
  		 double [] feed = out.clone();
  		 retCode = c1.stdDev(0, length.value-1, feed, pd, 1, begin, length, out);
  		 if (retCode == RetCode.Success) {
  			 dev = (float)out[length.value-1];
  			 
  		 }
        }
        
        return new float[] {val,dev};
	}

	private ArrayList<TickData> getweektick(ArrayList<TickData> tickhash, int interval2) {
		// TODO Auto-generated method stub
		Calendar cal1 = Calendar.getInstance();
		ArrayList <TickData> result = new ArrayList<TickData>();        		
		int currentweek= 0;
		float openprice = 0,closeprice = 0,lowprice = 0,highprice = 0,volume =0f;	
		Date tickstarttime = null,tickendtime;
						
						  for(int i=0 ; i< tickhash.size();i++) 
						{
						
						  TickData currdata = tickhash.get(i);
						   cal1.setTime(currdata.getTickend());
						    if(i==0)
							{
							 currentweek = cal1.get(Calendar.WEEK_OF_YEAR) ;
							 openprice = currdata.getOpenprice();
							 closeprice = currdata.getCloseprice();
							 lowprice = currdata.getLowprice();
							 highprice = currdata.getHighprice();
							 volume = currdata.getVolume();
							 
							}
							else if (i == tickhash.size())
							{
							TickData newdata = new TickData();
								 newdata.setOpenprice(openprice);
			        	  newdata.setCloseprice(closeprice);
			        	  newdata.setHighprice(highprice);
			        	  newdata.setLowprice(lowprice);
			        	  //newdata.setTickstart(tickstarttime);
			        	 // newdata.setTickend(tickendtime);
			        	  newdata.setVolume(volume);
						  result.add(newdata);
						  System.out.println("open:"+newdata.getOpenprice()+" High: "+newdata.getHighprice()+" low: "+newdata.getLowprice() +" Close: "+newdata.getCloseprice());
							}
							else
							{
							  if (currentweek == cal1.get(Calendar.WEEK_OF_YEAR))
							  {
							     if (currdata.getLowprice() < lowprice)
								   lowprice = currdata.getLowprice();
								 if (currdata.getHighprice() > highprice)
								 highprice = currdata.getHighprice();
								 closeprice = currdata.getCloseprice();
								 volume = volume + currdata.getVolume();
							  }
							  else
							  {
							     TickData newdata = new TickData();
								 newdata.setOpenprice(openprice);
			        	  newdata.setCloseprice(closeprice);
			        	  newdata.setHighprice(highprice);
			        	  newdata.setLowprice(lowprice);
			        	  //newdata.setTickstart(tickstarttime);
			        	 // newdata.setTickend(tickendtime);
			        	  newdata.setVolume(volume);
						  result.add(newdata);
						  System.out.println("open:"+newdata.getOpenprice()+" High: "+newdata.getHighprice()+" low: "+newdata.getLowprice() +" Close: "+newdata.getCloseprice());
						  currentweek = cal1.get(Calendar.WEEK_OF_YEAR) ;
							 openprice = currdata.getOpenprice();
							 closeprice = currdata.getCloseprice();
							 lowprice = currdata.getLowprice();
							 highprice = currdata.getLowprice();
							 volume = currdata.getVolume();
						  
						  
						  
							  }
							  
							}
						
						
						}
						  return result;
	}

	private void updateweeklycamarilla(Connection conn, float open, float high, float low,
			float close, String stocksymbol) {
		// TODO Auto-generated method stub
		float  range = high - low;
		float s1 = (float) (close - ((range * 1.1) / 12));
		float s2 = (float) (close - ((range * 1.1) / 6));
		float s3 = (float) (close - ((range * 1.1) / 4));
		float s4 = (float) (close - ((range * 1.1) / 2));
		float r1 = (float) (close + ((range * 1.1) / 12));
		float r2 = (float) (close + ((range * 1.1) / 6));
		float r3 = (float) (close + ((range * 1.1) / 4));
		float r4 = (float) (close + ((range * 1.1) / 2));
		
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update Camarillaweekpivot set s1=?,s2=?,s3=?,s4=?,r1=?,r2=?,r3=?,r4=? where stocksymbol=?");
			ps.setFloat(1,s1);
			ps.setFloat(2,s2);
			ps.setFloat(3,s3);
			ps.setFloat(4,s4);
			
			ps.setFloat(5,r1);
			ps.setFloat(6,r2);
			ps.setFloat(7,r3);
			ps.setFloat(8,r4);
			ps.setString(9, stocksymbol);
			
			int rowsupdated = ps.executeUpdate();
			
			if(rowsupdated ==0)
			{
				ps.close();
				ps = conn.prepareStatement("insert into Camarillaweekpivot (s1,s2,s3,s4,r1,r2,r3,r4,stocksymbol) values (?,?,?,?,?,?,?,?,?)");
				ps.setFloat(1,s1);
				ps.setFloat(2,s2);
				ps.setFloat(3,s3);
				ps.setFloat(4,s4);
				
				ps.setFloat(5,r1);
				ps.setFloat(6,r2);
				ps.setFloat(7,r3);
				ps.setFloat(8,r4);
				ps.setString(9, stocksymbol);
				ps.executeUpdate();
				
			}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				ps.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		
		
		
		
	}

	private void updatemonthlycamarilla(Connection conn, float open, float high, float low,
			float close, String stocksymbol) {
		// TODO Auto-generated method stub
		float  range = high - low;
		float s1 = (float) (close - ((range * 1.1) / 12));
		float s2 = (float) (close - ((range * 1.1) / 6));
		float s3 = (float) (close - ((range * 1.1) / 4));
		float s4 = (float) (close - ((range * 1.1) / 2));
		float r1 = (float) (close + ((range * 1.1) / 12));
		float r2 = (float) (close + ((range * 1.1) / 6));
		float r3 = (float) (close + ((range * 1.1) / 4));
		float r4 = (float) (close + ((range * 1.1) / 2));
		
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement("update Camarillamonthlypivot set s1=?,s2=?,s3=?,s4=?,r1=?,r2=?,r3=?,r4=? where stocksymbol=?");
			ps.setFloat(1,s1);
			ps.setFloat(2,s2);
			ps.setFloat(3,s3);
			ps.setFloat(4,s4);
			
			ps.setFloat(5,r1);
			ps.setFloat(6,r2);
			ps.setFloat(7,r3);
			ps.setFloat(8,r4);
			ps.setString(9, stocksymbol);

			int rowsupdated = ps.executeUpdate();
			
			if(rowsupdated ==0)
			{
				ps.close();
				ps = conn.prepareStatement("insert into Camarillamonthlypivot (s1,s2,s3,s4,r1,r2,r3,r4,stocksymbol) values (?,?,?,?,?,?,?,?,?)");
				ps.setFloat(1,s1);
				ps.setFloat(2,s2);
				ps.setFloat(3,s3);
				ps.setFloat(4,s4);
				
				ps.setFloat(5,r1);
				ps.setFloat(6,r2);
				ps.setFloat(7,r3);
				ps.setFloat(8,r4);
				ps.setString(9, stocksymbol);
				ps.executeUpdate();
				
			}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				ps.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
		
		
		
		
		
	}

	private float[] getsupertrend7(float[] closeArray, float[] highArray, float[] lowArray, Core c) {
		// TODO Auto-generated method stub
		
			

return new float[] { 0.0f, 0.0f,0.0f };
		
		
		


	}

	private float[] getsupertrend(float[] closeArray, float[] highArray, float[] lowArray, Core c, String actualinterval2, String trensetting) {
		// TODO Auto-generated method stub
		
		double[] out = new double[7000];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        
        int period =12;
        float factor = 8;
        
        if(trensetting != null)
        {
        	if(trensetting.contains("-"))
        	{
        		String settingsar[] = trensetting.split("-");
        		period = Integer.parseInt(settingsar[0]);
        		
        		// factor = Integer.parseInt(settingsar[1]);
        		factor = Float.valueOf(settingsar[1]);
        		System.out.println(period+"period"+":"+factor+"factor");
        	}
        }
        String periodVar = System.getenv("STREND_PERIOD");
        String factorVar = System.getenv("STREND_FACTOR");
        
    //    int period =12, factor = 8;
        
        if (periodVar != null )
        	if(periodVar.length() >=1)
        	{
        		System.out.println("read period from env variable as : "+periodVar);
        		period = Integer.parseInt(periodVar);
        	}
        
        if (factorVar != null )
        	if(factorVar.length() >=1)
        	{
        		System.out.println("read factor from env variable as : "+factorVar);
        		factor = Float.valueOf(factorVar);
        	}
        
        
        
		float up=0,dn=0;
		float trendup=0,trenddown=0,trend=0,atr=0;
		for(int i=0;i <closeArray.length;i++)
		{
			if(i>=period)
			{
			up = ((highArray[i]+lowArray[i])/2);
			dn = ((highArray[i]+lowArray[i])/2);
			RetCode retCode =c.atr(0, i, highArray, lowArray, closeArray, period, begin, length, out);
   		 if (retCode == RetCode.Success) {
   			 up = (float) (up-(factor*out[length.value-1]));
   			dn = (float) (dn+(factor*out[length.value-1]));
   			 
   		 }
   		 if (closeArray[i-1] > trendup )
   		 {
   			 if(up > trendup)
   				 trendup = up;
   		 }
   		 else
   			 trendup = up;
   		 
   		if (closeArray[i-1] < trenddown )
  		 {
  			 if (dn < trenddown)
  				 trenddown = dn;
  		 }
   		else
   			trenddown = dn;
   		 
   		if (closeArray[i] > trenddown)
   			trend =1;
   		if (closeArray[i]< trendup)
   			trend = -1;
   		
			}//if end
		}

return new float[] { trenddown, trendup,trend };
		
		
		
		//return (Double) null;
	}

	private ArrayList<TickData> getconvertedtick(ArrayList<TickData> tickhash, int interval) {
		// TODO Auto-generated method stub
		ArrayList <TickData> result = new ArrayList<TickData>();
		HashMap <Integer,String> timeconfig = new HashMap <Integer,String>();
			timeconfig = gettimeconfig(interval);
		
				//String starttimeconfig = getstarttimeconfig(interval);
				//String endtimeconfig = getendtimeconfig(interval);
		float openprice = 0,closeprice = 0,lowprice = 0,highprice = 0,volume =0f;	
		Date tickstarttime = null,tickendtime;
	    String configendtime = null;
	    boolean tickstart = false;
				for(int i=0 ; i< tickhash.size();i++) 
				{
					
	       TickData currdata = tickhash.get(i);
	       Calendar starttimecal = GregorianCalendar.getInstance();
	       starttimecal.setTime(currdata.getTickstart());
	       Calendar endtimecal = GregorianCalendar.getInstance();
	       endtimecal.setTime(currdata.getTickend());
	       String sttime = new String();
	       sttime = starttimecal.get(Calendar.HOUR_OF_DAY)+""+starttimecal.get(Calendar.MINUTE);
	       if (sttime.length() < 4)
	    	   sttime = sttime+"0";
	      Integer sttimeint = Integer.parseInt(sttime);
	       String ettime = new String();
	       
	       ettime = endtimecal.get(Calendar.HOUR_OF_DAY)+""+endtimecal.get(Calendar.MINUTE);
	       if (ettime.length() <4)
	    	   ettime = ettime+"0";
	       boolean containskey = (timeconfig.containsKey(sttimeint));
	        if (containskey)
	        {
	        	volume=0f;
	        	tickstart = true;
	        	configendtime = (String)timeconfig.get(sttimeint);
	            openprice = currdata.getOpenprice();
	            highprice = currdata.getHighprice();
	            lowprice = currdata.getLowprice();
	            closeprice = currdata.getCloseprice();
	            volume = volume + currdata.getVolume();
	            tickstarttime = currdata.getTickstart();
	        }
	        
	        if(tickstart && configendtime.equals(ettime))
	        {
	        	tickstart = false;
	        	closeprice = currdata.getCloseprice();
	        	 volume = volume + currdata.getVolume();
	        	 
	        	if (lowprice > currdata.getLowprice())
	        		lowprice = currdata.getLowprice();
	        	if (highprice < currdata.getHighprice())
	        	    highprice = currdata.getHighprice();
	        	   
	        	    tickendtime = currdata.getTickend();
	        	 TickData newdata = new TickData();
	        	  newdata.setOpenprice(openprice);
	        	  newdata.setCloseprice(closeprice);
	        	  newdata.setHighprice(highprice);
	        	  newdata.setLowprice(lowprice);
	        	  newdata.setTickstart(tickstarttime);
	        	  newdata.setTickend(tickendtime);
	        	  newdata.setVolume(volume);
	        // System.out.println(newdata.getTickstart()+"::"+newdata.getTickend()+"::"+newdata.getOpenprice()+"::"+newdata.getHighprice()+"::"+newdata.getLowprice()+"::"+newdata.getCloseprice());
	        	  result.add(newdata);
	        	  }
	        
	        else if (tickstart && !containskey)
	        {
	        	volume = volume + currdata.getVolume();
	        	 
	        	if (lowprice > currdata.getLowprice())
	        		lowprice = currdata.getLowprice();
	        	if (highprice < currdata.getHighprice())
	        	    highprice = currdata.getHighprice();
	        	    
	        	    if (i == (tickhash.size()-1))
	        	    {
	        	    	tickendtime = currdata.getTickend();
	   	        	 TickData newdata = new TickData();
	   	        	  newdata.setOpenprice(openprice);
	   	        	  newdata.setCloseprice(closeprice);
	   	        	  newdata.setHighprice(highprice);
	   	        	  newdata.setLowprice(lowprice);
	   	        	  newdata.setTickstart(tickstarttime);
	   	        	  newdata.setTickend(tickendtime);
	   	        	  newdata.setVolume(volume);
	   	        //	System.out.println(newdata.getTickstart()+"::"+newdata.getTickend()+"::"+newdata.getOpenprice()+"::"+newdata.getHighprice()+"::"+newdata.getLowprice()+"::"+newdata.getCloseprice());
	   		        	  
	   	        	result.add(newdata);
	   	        	tickstart = false;
	   	        	
	        	    }
	        	    
	        }
	        
	    }
				
	    return result;
	}

	private String getendtimeconfig(int interval2) {
		// TODO Auto-generated method stub
String minend15 = "1200,1215,1230,1245,1300,1315,1330,1345,1400,1415,1430,1445,1500,1515,1530,1545,1600,1615,1630,1645,1700,1715,1730,1745,1800";
		
		
		return minend15;
	}

	private String getstarttimeconfig(int interval) {
		// TODO Auto-generated method stub
		String minstart15 = "1145,1200,1215,1230,1245,1300,1315,1330,1345,1400,1415,1430,1445,1500,1515,1530,1545,1600,1615,1630,1645,1700,1715,1730,1745";
		
		
		return minstart15;
	}

	private HashMap<Integer, String> gettimeconfig(int interval) {
		// TODO Auto-generated method stub
		HashMap <Integer,String> configdata15 = new HashMap <Integer,String>();
		configdata15.put(1145, new String("1200"));
		configdata15.put(1200, new String("1215"));
		configdata15.put(1215, new String("1230"));
		configdata15.put(1230, new String("1245"));
		configdata15.put(1245, new String("1300"));
		configdata15.put(1300, new String("1315"));
		configdata15.put(1315, new String("1330"));
		configdata15.put(1330, new String("1345"));
		configdata15.put(1345, new String("1400"));
		configdata15.put(1400, new String("1415"));
		configdata15.put(1415, new String("1430"));
		configdata15.put(1430, new String("1445"));
		configdata15.put(1445, new String("1500"));
		configdata15.put(1500, new String("1515"));
		configdata15.put(1515, new String("1530"));
		configdata15.put(1530, new String("1545"));
		configdata15.put(1545, new String("1600"));
		configdata15.put(1600, new String("1615"));
		configdata15.put(1615, new String("1630"));
		configdata15.put(1630, new String("1645"));
		configdata15.put(1645, new String("1700"));
		configdata15.put(1700, new String("1715"));
		configdata15.put(1715, new String("1730"));
		configdata15.put(1730, new String("1745"));
		configdata15.put(1745, new String("1800"));
		
		HashMap <Integer,String> configdata30 = new HashMap <Integer,String>();
		configdata30.put(1145, new String("1215"));
		configdata30.put(1215, new String("1245"));
		configdata30.put(1245, new String("1315"));
		configdata30.put(1315, new String("1345"));
		configdata30.put(1345, new String("1415"));
		configdata30.put(1415, new String("1445"));
		configdata30.put(1445, new String("1515"));
		configdata30.put(1515, new String("1545"));
		configdata30.put(1545, new String("1615"));
		configdata30.put(1615, new String("1645"));
		configdata30.put(1645, new String("1715"));
		configdata30.put(1715, new String("1745"));
		configdata30.put(1745, new String("1800"));
		
		HashMap <Integer,String> configdata60 = new HashMap <Integer,String>();
		configdata60.put(1145, new String("1245"));
		configdata60.put(1245, new String("1345"));
		configdata60.put(1345, new String("1445"));
		configdata60.put(1445, new String("1545"));
		configdata60.put(1545, new String("1645"));
		configdata60.put(1645, new String("1745"));
		configdata60.put(1745, new String("1800"));
		
		HashMap <Integer,String> configdata120 = new HashMap <Integer,String>();
		configdata120.put(1145, new String("1345"));
		configdata120.put(1345, new String("1545"));
		configdata120.put(1545, new String("1745"));
		configdata120.put(1745, new String("1800"));
		
		if ((interval/60) == 15)
		return configdata15;
		
		else if ((interval/60) == 30)
			return configdata30;
		
		else if ((interval/60) == 60)
			return configdata60;
		
		else
			return configdata120;
		
		
	}

	private static double[] getstochvals(float[] lowdiffArray, float[] highdiffArray, float[] obvArray, float[] lowArray,
			RetCode retCode, Core c) {
		 double[] outFastK = new double[7000];
			double[] outFastD = new double[7000];
			MInteger outNBElement = new MInteger(); 
			MInteger outBegIdx = new MInteger(); 
			
			double lowk=0,lowd=0,highk=0,highd=0,volk = 0,vold=0,pricek = 0,priced=0,finalk = 0,finald = 0;
			/*double[] outMin = null;
			double[] outMax = null;
			retCode = c.minMax(0, lowdiffArray.length-1, lowdiffArray, 14, outBegIdx, outNBElement, outMin, outMax);
			*/
			retCode = c.stochF(0, lowdiffArray.length-1, lowdiffArray, lowdiffArray, lowdiffArray, 14, 3, MAType.Wma, outBegIdx, outNBElement, outFastK, outFastD);
			 if (retCode == RetCode.Success) {
				 try{
				 lowk=outFastK[outNBElement.value-1] ;
				 lowd=outFastD[outNBElement.value-1];
				 }
				 catch(Exception e)
				 {
					 lowk=75;
					 lowd=75;
				 }
				// System.out.println(outFastK[outNBElement.value-1]+"::"+outFastD[outNBElement.value-1]);
			 }
			 
			 retCode = c.stochF(0, highdiffArray.length-1, highdiffArray, highdiffArray, highdiffArray, 14, 3, MAType.Wma, outBegIdx, outNBElement, outFastK, outFastD);
			 if (retCode == RetCode.Success) {
				 try{
				 highk= outFastK[outNBElement.value-1] ;
				 highd=outFastD[outNBElement.value-1];
				 }
				 catch(Exception e)
				 {
					 highk=75;
					 highd=75;
				 }
				 //System.out.println(outFastK[outNBElement.value-1]+"::"+outFastD[outNBElement.value-1]);
			 }
			 
			 retCode = c.stochF(0, obvArray.length-1, obvArray, obvArray, obvArray, 14, 3, MAType.Wma, outBegIdx, outNBElement, outFastK, outFastD);
		
			 if (retCode == RetCode.Success) {
				 try
				 {
				 volk=outFastK[outNBElement.value-1] ;
				 vold=outFastD[outNBElement.value-1];
				 }
				 catch(Exception e)
				 {
					 volk=75;
					 vold=75;
				 }
			//	System.out.println(outFastK[outNBElement.value-1]+"::"+outFastD[outNBElement.value-1]);
			 }
			 
			 retCode = c.stochF(0, lowArray.length-1, lowArray, lowArray, lowArray, 14, 5, MAType.Wma, outBegIdx, outNBElement, outFastK, outFastD);
				
			 if (retCode == RetCode.Success) {
				 try
				 {
				pricek=outFastK[outNBElement.value-1];
				priced=outFastD[outNBElement.value-1];
				 }
				 catch(Exception e)
				 {
					 pricek=75;
					 priced=75;
				 }
				// System.out.println(outFastK[outNBElement.value-1]+"::"+outFastD[outNBElement.value-1]);
			 }
		
			 finalk = (volk+pricek)/2;
			 finald= (vold+((lowk+lowd+highk+highd+pricek+priced)/6))/2;
			 
			 
			 return (new double[]{finalk,finald});
			 
			 
	}


	private static Float getobvvol(Float c_highprice, Float c_lowprice, Float c_closeprice, Float c_currentvolume,
			Float obvvolume, Float closeprice) {
		// TODO Auto-generated method stub
		
		Float range = (c_highprice+c_lowprice)/2;
		if (c_closeprice >= range)
		{
			if (c_closeprice >= closeprice)
			{
				return (float) (obvvolume + c_currentvolume);
			}
			else
			{
				return obvvolume;
			}
		}
		else
		{
			if (c_closeprice >= closeprice)
			{
				return obvvolume ;
			}
			else
			{
				return obvvolume - c_currentvolume;
			}
		}
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		ArrayList <String> stocklist = new ArrayList <String> ();
		if(this.actualinterval.equals("600") || this.actualinterval.equals("300") || this.actualinterval.equals("900") )
			stocklist = getnse500fromdb();
		else
		   stocklist = getnse200fromdb();
		
Iterator<String> it = stocklist.iterator();
		
Dataconn dataconn =new Dataconn();
Connection conn = dataconn.getconn();
PreparedStatement stmt = null;
System.out.println("using table "+"nse"+this.actualinterval+"min");
String updatestatement = "update nse"+this.actualinterval+"min set stochk=?,stochd=?,WillR=?,rsi=?,Trend5up=?,Trend5down=?,Trendstatus=? where stocksymbol=?";		
String insertstatement = "insert into nse"+this.actualinterval+"min (stocksymbol,stochk,stochd,willr,rsi,Trend5up,Trend5down,Trendstatus) values (?,?,?,?,?,?,?,?)";
while (it.hasNext()) {
			
	        String stocksymbol = (String) it.next();
	        try {
				StockOtherTechnicals st = getstochdata(stocksymbol,conn);
				if(st.getDownvalue() == 0)
					continue;
				stmt = conn.prepareStatement(updatestatement);
				stmt.setFloat(1, (float) st.getDayk());
				stmt.setFloat(2, (float) st.getDayd());
				stmt.setFloat(3, (float) st.getDaywilliamsr());
				stmt.setFloat(4, (float) st.getDayrsi());
				stmt.setFloat(5, (float) st.getUpvalue());
				stmt.setFloat(6, (float) st.getDownvalue());
				stmt.setString(7, st.getTrend());
				stmt.setString(8, stocksymbol);
		//	System.out.println("trying update of "+ stocksymbol);
				int rowsUpdated = stmt.executeUpdate();
			//	System.out.println("rows updated" + rowsUpdated);
				if (rowsUpdated == 0) {
				//	System.out.println("Update failed so inserting");
					stmt.close();
					stmt = conn.prepareStatement(insertstatement);
					stmt.setString(1, stocksymbol);
					stmt.setFloat(2, (float) st.getDayk());
					stmt.setFloat(3, (float) st.getDayd());
					stmt.setFloat(4, (float) st.getDaywilliamsr());
					stmt.setFloat(5, (float) st.getDayrsi());
					stmt.setFloat(6, (float) st.getUpvalue());
					stmt.setFloat(7, (float) st.getDownvalue());
					stmt.setString(8, st.getTrend());
					stmt.executeUpdate();
					
					
				}
				
				stmt.close();
				
			} catch (MalformedURLException | ParseException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					stmt.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
	        
		}
dataconn.closeconn();
	}
	
	private ArrayList<String> getnse500fromdb() {
		// TODO Auto-generated method stub
		Dataconn dataconn =new Dataconn();
		Connection conn = dataconn.getconn();
		ArrayList <String> nse200  =new ArrayList <String>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select STOCKSYMBOL from nse500");
			while (rs.next()) {

				nse200.add(rs.getString(1));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nse200;
	}

	private ArrayList <String> getnse200fromdb() {
		// TODO Auto-generated method stub
		Dataconn dataconn =new Dataconn();
		Connection conn = dataconn.getconn();
		ArrayList <String> nse200  =new ArrayList <String>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select STOCKSYMBOL from nse200");
			while (rs.next()) {

				nse200.add(rs.getString(1));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nse200;
}
}