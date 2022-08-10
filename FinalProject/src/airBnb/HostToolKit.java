package airBnb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class HostToolKit {
	
	private static String url = "jdbc:mysql://127.0.0.1/airbnb";
	private static String uname = "root";
	private static String pword = "*A18273645a*";
	
	private String city, country, price;
	private String[] amenities;
	
	public HostToolKit(String city, String country)
	{
		this.city = city;
		this.country = country;
		this.price = Float.toString(priceListing());
		this.amenities = suggestedAmenities();
	}
	
	/* Takes in a regular (no insert/delete/update) query and executes it
	 * Returns the results of the query on success and null on failure
	 * */
	private static ResultSet executeQuery(String query)
	{
		ResultSet res;
		try {
			Connection con = DriverManager.getConnection(url, uname, pword);
			Statement statement = con.createStatement();
			res = statement.executeQuery(query);
		}
		catch(SQLException e) {
			e.printStackTrace();
			System.out.println("Request failed");
			res = null;
		}
		return res;
		
	}
	
	private float priceListing()
	{
		try
		{
			// check if this is the first listing in this city
			ResultSet res = executeQuery(String.format("SELECT * FROM Listing WHERE city='%s' AND country='%s'", this.city, this.country));
			if(!res.next())
			{
				return 100;
			}
			
			// find the average price of the top listings in a given city in a country
			res = executeQuery(String.format("SELECT COUNT(*) FROM Listing as L JOIN Reservation as R ON L.address=R.listingaddress and L.postalcode = R.listingpostalcode "
								   				 + "WHERE city='%s' AND country='%s'", this.city, this.country));
			// total number of listings in a given city in a country
			res.next();
			int numListings = res.getInt("COUNT(*)");
			
			// 10% of those listings
			numListings = (int) Math.ceil(numListings / 10.0);
			
			// query that finds the top 10% of listings in a city in a country ranked by number of reservations
			// and gets the average of their price
			String query = String.format(" SELECT AVG(pricepernight) FROM "
									 + "("
									 + "SELECT address, postalcode, pricepernight "
									 + "FROM listing as L JOIN reservation as R ON L.address=R.listingaddress AND L.postalcode = R.listingpostalcode "
									 + "WHERE city='%s' AND country='%s' "
									 + "GROUP BY address, postalcode "
									 + "ORDER BY COUNT(*) DESC "
									 + "LIMIT %s"
									 + ") as topListings", this.city, this.country, Integer.toString(numListings));
			res = executeQuery(query);
			return res.getFloat("AVG(pricepernight)");
		}
		// if an error is caught, it means this is the first listing to be placed in a country
		// in which case the default price suggestion will be 100
		catch(SQLException e)
		{
			return 100;
		}
		
	}
	
	private String[] suggestedAmenities()
	{
		// find the average price of the top listings in a given city in a country
				ResultSet res = executeQuery(String.format("SELECT COUNT(*) FROM Listing as L JOIN Reservation as R ON L.address=R.listingaddress and L.postalcode = R.listingpostalcode "
									   				 + "WHERE city='%s' AND country='%s'", this.city, this.country));
		try
		{
			// total number of listings in a given city in a country
			res.next();
			int  numListings = res.getInt("COUNT(*)");
			
			// 10% of those listings
			numListings = (int) Math.ceil(numListings / 10.0);
			
			String query = String.format("SELECT DISTINCT amenity "
					+ "FROM offers as O "
					+ "WHERE EXISTS"
					+ "    ("
					+ "    SELECT * "
					+ "	   FROM reservation as R "
					+ "	   WHERE R.listingaddress=O.listingaddress AND R.listingpostalcode=O.listingpostalcode AND EXISTS "
					+ "    		(SELECT * "
					+ "			 FROM listing as L "
					+ "			 WHERE L.address=R.listingaddress AND L.postalcode = R.listingpostalcode AND city='%s' AND country='%s'"
					+ "			)"
					+ "    GROUP BY R.listingaddress,R.listingpostalcode "
					+ "	   ORDER BY COUNT(*) DESC LIMIT %s"
					+ "    );",
					this.city, this.country, numListings);
			res = executeQuery(query);
			
			String[] amenities = new String[Operations.getAmenities().length];
			
			// store the amenities found and return them
			int i = 0;
			if(res.next())
			{
				amenities[i] =res.getString("amenity");
				i++;
				while(res.next())
				{
					amenities[i] =res.getString("amenity");
					i++;
				}
				
				return amenities;
			}
			return new String[] {"Wifi", "TV", "Washer", "Dryer"};
		}
		catch (SQLException e)
		{
			// default amenities
			return new String[] {"Wifi", "TV", "Washer", "Dryer"};
		}
	}
	
	public String getPriceSuggestion()
	{
		return this.price;
	}
	
	public String getAmenitySuggestions()
	{
		String rData = "";
		
		if (this.amenities[0] != null)
		{
			rData += this.amenities[0];
		}
		int i = 1;
		while(i < this.amenities.length && this.amenities[i] != null)
		{
			rData += "," + this.amenities[i];
			i += 1;
		}
		return rData;
	}
}
