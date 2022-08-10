package airBnb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Operations {
	private static String url = "jdbc:mysql://127.0.0.1/airbnb";
	private static String uname = "root";
	private static String pword = "*A18273645a*";
	
	private static enum Ops
	{
			USER_CREATE("INSERT INTO User(SIN, firstName, lastName, dob, address, occupation, password, username) VALUES "),
			USER_DELETE("DELETE FROM User WHERE SIN="),
			USER_UPDATE("UPDATE User "),
			USER_FIND("SELECT * FROM User WHERE SIN="),
			RENTER_CREATE("INSERT INTO Renter(SIN) VALUES "),
			RENTER_DELETE("DELETE FROM Renter WHERE SIN="),
			RENTER_FIND("SELECT SIN FROM Renter WHERE SIN="),
			HOST_CREATE("INSERT INTO Host(SIN) VALUES "),
			HOST_DELETE("DELETE FROM Host WHERE SIN="),
			HOST_FIND("SELECT SIN FROM Host WHERE SIN="),
			LISTING_CREATE("INSERT INTO Listing(address, postalCode, city,country, type, latitude, longitude, pricePerNight, host) VALUES "),
			LISTING_DELETE("DELETE FROM Listing WHERE "),
			LISTING_FIND("SELECT * FROM Listing WHERE "),
			LISTING_UPDATE("UPDATE Listing SET "),
			RESERVATION_BOOK("INSERT INTO Reservation(renterSIN, listingAddress, listingPostalCode, payment, checkIn, checkOut, totalPrice) VALUES "),
			RESERVATION_CANCEL("UPDATE Reservation SET isCancelled=1 WHERE "),
			RESERVATION_FIND("SELECT * FROM Reservation WHERE iscancelled=1 AND "),
			RESERVATION_HOST_FIND("SELECT R.* FROM Listing AS L JOIN Reservation AS R on L.address=R.listingaddress and L.postalCode=R.listingPostalCode WHERE "),
			DATE_RANGE_CREATE("INSERT INTO DateRange(startDate, endDate) VALUES "),
			UNAVAILABILITY_CREATE("INSERT INTO Unavailability(listingAddress, listingPostalCode, startDate, endDate) VALUES "),
			UNAVAILABILITY_DELETE("DELETE FROM Unavailability WHERE "),
			UNAVAILABILITY_FIND("SELECT * FROM Unavailability WHERE "),
			LISTING_REVIEW_CREATE("INSERT INTO ListingReview(renterSIN, listingAddress, listingPostalCode, comment, rating) VALUES "),
			RENTER_REVIEW_CREATE("INSERT INTO RenterReview(hostSIN, renterSIN, comment, rating) VALUES "),
			LISTING_AMENITY_OFFER_CREATE("INSERT INTO Offers(listingAddress, listingPostalCode, amenity) VALUES");
			
			private String syntax;
			Ops(String syntax){
				this.syntax = syntax;
			}
			
			public String toString() {
				return this.syntax;
			}
	}

	/* Takes in a regular (no insert/delete/update) query and executes it
	 * Returns the results of the query on success and null on failure
	 * */
	private static ResultSet executeOperation(String query)
	{
		ResultSet res;
		try {
			Connection con = DriverManager.getConnection(url, uname, pword);
			Statement statement = con.createStatement();
			res = statement.executeQuery(query);
		}
		catch(SQLException e) {
			System.out.println("Request failed");
			res = null;
		}
		return res;
		
	}
	
	/* Takes in an updating query (insert/delete/update) and executes it
	 * Returns 1 on success and 0 on failure
	 * */
	private static int executeModification(String query)
	{
		int exit_status;
		try {
			Connection con = DriverManager.getConnection(url, uname, pword);
			Statement statement = con.createStatement();
			statement.executeUpdate(query);
			exit_status = 1;
			statement.close();
			con.close();
		}
		catch(SQLException e) {
			System.out.println("Request failed");
			exit_status = 0;
		}
		return exit_status;
	}
	
	/*
	 * Creates a new user and adds them to the renter/host database depending on their category
	 * Returns 1 on success and 0 on failure
	 */
	public static int createUser(String SIN, String firstName, String lastName, String username, String dob, String address,
			String occupation, String password, boolean isHost, boolean isRenter)
	{
		int exit_status = 1;
		String newUserQuery = Ops.USER_CREATE.toString() + String.format(
				"(%s, '%s', '%s', '%s', '%s', '%s', '%s', '%s')", SIN, firstName, lastName, dob, address, occupation, password, username);
		exit_status *= executeModification(newUserQuery);
		
		if (isHost)
		{
			exit_status *= executeModification(Ops.HOST_CREATE.toString() + String.format("(%s)", SIN));
		}
		if (isRenter)
		{
			exit_status *= executeModification(Ops.RENTER_CREATE.toString() + String.format("(%s)", SIN));
		}
		return exit_status;
	}
	
	/*
	 * Deletes the user with <SIN> and also removes them from the renter/host database
	 * if they were in either of those categories
	 */
	public static void deleteUser(String SIN)
	{
		String deleteUserQuery = Ops.USER_DELETE.toString() + SIN;
		String deleteRenterQuery = Ops.RENTER_DELETE.toString() + SIN;
		String deleteHostQuery = Ops.HOST_DELETE.toString() + SIN;

		executeModification(deleteUserQuery);
		executeModification(deleteRenterQuery);
		executeModification(deleteHostQuery);
	}
	
	/*
	 * Updates the specified info about a user
	 */
	public static void updateUserInfo(String field, String newData, String condition)
	{
		String userUpdateQuery = Ops.USER_UPDATE.toString() + String.format("SET %s=%s WHERE %s", field, newData, condition);
		executeModification(userUpdateQuery);
	}
	
	/*
	 * Checks if a user with sin <SIN> exists, if they do, checks that <password> is the same as the
	 * one stored in the database
	 * Returns -1 if user does not exist, 0 if login failed, and the users sin number on success
	 */
	public static int login(String SIN, String password)
	{
		try
		{
			ResultSet res = executeOperation(Ops.USER_FIND + SIN);
			// user exists
			if(res.next())
			{
				return res.getString("password").trim().equalsIgnoreCase(password.trim()) ? Integer.parseInt(SIN) : 0;
			}
			// user does not exist
			return -1;
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return 0;
		}
	}
	
	/*
	 * Returns the name of the user with <userSIN>
	 * Returns the empty string if user does not exist
	 */
	public static String getUserName(String userSIN)
	{
		try
		{
			ResultSet res = executeOperation(Ops.USER_FIND + userSIN);
			if(res.next())
			{
				return res.getString("firstName") + " " + res.getString("lastName");
			}
			return "";
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return "";
		}
	}
	
	/*
	 * Creates a listing associated with a host
	 * Returns 1 on success, 0 on failure and -1 if the user is not a host 
	 */
	public static int createListing(String address, String postalCode,String city, String country,
			String type, String latitude, String longitude, String pricePerNight, String host, String amenities)
	{
		// first check that the user creating this listing is a host
		if (isHost(host))
		{
			int createListingStatus = executeModification(Ops.LISTING_CREATE.toString() + String.format("('%s', '%s', '%s', '%s', '%s', %s, %s , %s, %s)",
					address, postalCode, city, country, type, latitude, longitude, pricePerNight, host));
			int createListingAmenitiesStatus = createListingAmenities(address, postalCode, amenities);
			return createListingStatus * createListingAmenitiesStatus;
		}
		return -1;
	}
	
	/*
	 * Associates listing with <address> and <postalCode> with the amenities provided in <amenities>
	 * 
	 * precondition: <amenities> is a string with comma separated values of the names of amenities
	 */
	private static int createListingAmenities(String address, String postalCode, String amenities)
	{
		String[] names = amenities.split(",");
		int exit_status = 1;
		for(int i = 0; i < names.length; i++)
		{
			String query = String.format("%s ('%s', '%s', '%s')",Ops.LISTING_AMENITY_OFFER_CREATE, address, postalCode, names[i]);
			exit_status *= executeModification(query);
		}
		return exit_status;
	}
	public static String[] getAmenities()
	{
		String[] amenities = new String[getNumAmenities()];
		int i = 0;
		try	
		{
			ResultSet res = executeOperation("SELECT * from amenity");
			while(res.next())
			{
				amenities[i] = res.getString("name");
				i++;
			}
			return amenities;
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return null;
		}
	}
	
	private static int getNumAmenities()
	{
		try
		{
			ResultSet res = executeOperation("SELECT COUNT(*) FROM Amenity");
			res.next();
			return res.getInt("COUNT(*)");
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return 0;
		}
	}
	
	/*
	 * Checks if user with sin <SIN> is a host
	 * Returns true if the user is a host and false otherwise
	 */
	public static boolean isHost(String SIN)
	{
		try
		{
			return executeOperation(Ops.HOST_FIND.toString() + SIN).next();
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * Checks if user with sin <SIN> is a renter
	 * Returns true if the user is a renter and false otherwise
	 */
	public static boolean isRenter(String SIN)
	{
		try
		{
			return executeOperation(Ops.RENTER_FIND.toString() + SIN).next();
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * Removes a listing with <address> and <postalCode>
	 * Returns 1 on success and 0 on failure and -1 if the user making this request
	 * is not the host of the listing
	 */
	public static int removeListing(String address, String postalCode, String host)
	{
		// check that the listing even exists
		if(listingExists(address, postalCode))
		{
			if(listingBelongsToHost(address, postalCode, host))
			{
				executeModification(Ops.LISTING_DELETE.toString() + String.format("address='%s' AND postalCode='%s'", address, postalCode));
				return 1;
			}
			return -1;
		}
		return 0;
		
	}
	
	/*
	 * Returns true if a listing with the specified address and postal code exist and false otherwise
	 */
	private static boolean listingExists(String address, String postalCode)
	{
		try
		{
			return executeOperation(Ops.LISTING_FIND.toString() +  
					String.format("address='%s' AND postalCode='%s'", address, postalCode)).next();
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * precondition: a listing with <address> and <postalCode> already exists
	 * Checks that the listing with <address> and <postalCode> belongs to the user with <SIN>
	 */
	private static boolean listingBelongsToHost(String address, String postalCode, String SIN)
	{
		try
		{
			ResultSet res = executeOperation(Ops.LISTING_FIND.toString() +  
					String.format("address='%s' AND postalCode='%s'", address, postalCode));
			res.next();
			return res.getString("host").trim().equalsIgnoreCase(SIN.trim());
		}
		catch (SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * Books a reservation for listing with <address> and <postalCode> for user with <renterSIN>
	 * return 1 on success and 0 on error
	 */
	public static int bookReservation(String renterSIN, String renterPayment, String checkIn, String checkOut,
			String address, String postalCode)
	{
		// if the user is a renter and they are not the same host as the listing's host, book the reservation
		if(isRenter(renterSIN) && !userIsListingHost(renterSIN, address, postalCode))
		{	
			// check that the listing is available during the date range before making the reservation
			if(listingIsAvailable(address, postalCode, checkIn, checkOut))
			{	
				// add to the range of unavailable dates for the listing
				createUnavailability(address, postalCode, checkIn, checkOut, null);
				
				// check the current price of the listing and save it so even if the host changes the price later, this transaction
				// does not change
				String totalPrice = getReservationTotalPrice(address, postalCode, checkIn, checkOut);

				// finally make the reservation
				return executeModification(Ops.RESERVATION_BOOK.toString() + String.format("(%s, '%s', '%s', '%s', '%s', '%s', %s)",
						renterSIN, address, postalCode, renterPayment, checkIn, checkOut, totalPrice));
			}
			System.out.println("Listing is not available in the date range requested");
			return 0;
		}
		return 0;
	}
	
	/*
	 * Checks if the listing has any reservations booked between <startDate> and <endDate>
	 * returns true if there is a booking and false otherwise
	 */
	private static boolean listingHasReservation(String address, String postalCode, String startDate, String endDate)
	{
		String query = String.format(Ops.RESERVATION_FIND.toString()
				+ "listingAddress='%s' AND listingPostalCode='%s' AND checkIn='%s' AND checkOut='%s'",
				address, postalCode, startDate, endDate);
		try
		{
			ResultSet res = executeOperation(query);
			if(res.next())
			{
				return true;
			}
			return false;
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * Checks if a listing is available in date range from <startDate> to <endDate>
	 * returns true if it is available and false otherwise
	 */
	private static boolean listingIsAvailable(String listingAddress, String listingPostalCode, String startDate, String endDate)
	{
		try
		{
			// check if either the starting date or the ending date requested are between a range of dates where the
			// listing is unavailable
			String condition = String.format("listingAddress='%s' AND listingPostalCode='%s' AND"
					+ "((startDate<='%s' AND '%s'<=endDate) OR (startDate<='%s' AND '%s'<=endDate))",
					listingAddress, listingPostalCode, startDate, startDate, endDate, endDate);
			ResultSet res = executeOperation(Ops.UNAVAILABILITY_FIND.toString() + condition);
			
			// the listing is booked some time between startDate and endDate
			if(res.next())
			{
				return false;
			}
			return true;
			
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	/*
	 * Creates a date range from startDate to endDate and stores it in the database
	 */
	private static int createDateRange(String startDate, String endDate)
	{
		return executeModification(Ops.DATE_RANGE_CREATE.toString() + String.format("('%s', '%s')", startDate, endDate));
	}
	
	/*
	 * Creates a data entry for a listing with <listingAddress> and <listingPostalCode> which is unavailable from <startDate> to <endDate>
	 * return 1 on success and 0 on error and -1 if the user is not the listing's host
	 */
	public static int createUnavailability(String listingAddress, String listingPostalCode, String startDate, String endDate, String userSIN)
	{
		// the only time the null value is passed in for userSIN is when the unavailability is being created through a renter reservation
		if(userSIN == null || userIsListingHost(userSIN, listingAddress, listingPostalCode))
		{	
			int dateRangeStatus = createDateRange(startDate, endDate);
			return dateRangeStatus * executeModification(Ops.UNAVAILABILITY_CREATE.toString() + String.format("('%s', '%s', '%s', '%s')",
					listingAddress, listingPostalCode, startDate, endDate));
		}
		return -1;
	}
	
	/*
	 * removes an unavailable time range from the database
	 * return 1 on success and 0 on failure
	 */
	public static int deleteUnavailability(String listingAddress, String listingPostalCode, String startDate, String endDate, String userSIN)
	{
		if(!listingHasReservation(listingAddress, listingPostalCode, startDate, endDate))
		{
			if(userIsListingHost(userSIN, listingAddress, listingPostalCode))
			{
				String query = String.format(Ops.UNAVAILABILITY_DELETE.toString()
						+ "listingAddress='%s' AND listingPostalCode='%s' AND startDate>='%s' AND endDate<='%s'",
						listingAddress, listingPostalCode, startDate, endDate);
				return executeModification(query);
			}
			System.out.println("Request denied: You are not the host of this listing");
			return -1;
		}
		System.out.println("Request denied: Listing has a booking in that time period");
		return 0;
	}
	
	/*
	 * Checks if <userSIN> is the same as the host for listing with <address> and <postalCode>
	 * returns true if <userSIN> is the same as the listing's host's and false otherwise
	 */
	private static boolean userIsListingHost(String userSIN, String listingAddress, String listingPostalCode)
	{
		try
		{
			ResultSet res = executeOperation(Ops.LISTING_FIND.toString() + String.format("address='%s' AND postalCode='%s'", listingAddress, listingPostalCode));
			if(res.next())
			{
				return res.getString("host").trim().equalsIgnoreCase(userSIN.trim());
			}
			return false;
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * Finds the price per night of a listing, as well as how many days the reservation will last, and returns the total price of the stay
	 */
	private static String getReservationTotalPrice(String listingAddress, String listingPostalCode, String checkIn, String checkOut)
	{
		try
		{
			String query = String.format("SELECT pricePerNight *  DATEDIFF('%s', '%s')"
					+ "FROM Listing WHERE address='%s' AND postalCode='%s' ",
					checkOut, checkIn, listingAddress, listingPostalCode);
			ResultSet res = executeOperation(query);
			if (res.next())
			{
				return res.getString(String.format("pricePerNight *  DATEDIFF('%s', '%s')", checkOut, checkIn));
			}
			return "0";
			
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return "0";
		}
	}
	
	/*
	 * Cancels the reservation made by user with <userSIN> on the listing with <address> and <postalCode>
	 * Returns 1 on success and 0 on failure
	 */
	public static int cancelReservation(String userSIN, String address, String postalCode, String checkIn, String checkOut)
	{
		// need to make sure the cancellation is being requested by either the renter or the host of the listing
		if(userIsListingRenter(userSIN, address, postalCode) || userIsListingHost(userSIN, address, postalCode))
		{
			String condition1 = String.format("listingAddress='%s' AND listingPostalCode='%s' AND startDate='%s' AND endDate='%s'",
					address, postalCode, checkIn, checkOut);
			executeModification(Ops.UNAVAILABILITY_DELETE.toString() + condition1);
			
			// cancel the reservation
			String condition2 = String.format("listingAddress='%s' AND listingPostalCode='%s'"
											+ "AND checkin='%s' AND checkOut='%s'",
											address, postalCode, checkIn, checkOut);
			executeModification(Ops.RESERVATION_CANCEL + condition2);
			
			return 1;
		}
		else
		{
			System.out.println("Request denied: you are not the renter or host of this listing");
			return 0;
		}
		
	}
	
	/*
	 * Checks if user with <userSIN> has rented the listing with <address> and <postalCode>
	 * return true if <userSIN> is a renter and false otherwise
	 */
	private static boolean userIsListingRenter(String userSIN, String listingAddress, String listingPostalCode)
	{
		try
		{
			String conditions = String.format("renterSIN=%s AND listingAddress='%s' AND listingPostalCode='%s'", userSIN, listingAddress, listingPostalCode);
			ResultSet res = executeOperation(Ops.RESERVATION_FIND.toString() + conditions);
			if(res.next())
			{
				return true;
			}
			return false;
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * Stores the comment and rating of renter with <renterSIN> on the listing with <listingAddress> and <listingPostalCode>
	 * returns 1 if the review was successfully uploaded and 0 otherwise
	 */
	public static int renterReviewOnListing(String renterSIN, String listingAddress, String listingPostalCode, String comment, String rating)
	{
		// check that the person inserting this comment is a renter that has rented this listing before and that
		// the stay has been completed
		if(userIsListingRenter(renterSIN, listingAddress, listingPostalCode) && 
				reservationIsComplete(renterSIN, listingAddress, listingPostalCode))
		{
			return executeModification(Ops.LISTING_REVIEW_CREATE.toString() + String.format("(%s, '%s', '%s', '%s', %s)",
					renterSIN, listingAddress, listingPostalCode, comment, rating));
		}
		System.out.println("You cannot comment unless you have rented this building before and until your stay is over");
		return 0;
		
	}
	
	/*
	 * Finds the earliest reservation made by renter with <renterSIN> on listing with <listingAddress> and <listingPostalCode>
	 * returns true if that reservation has already passed and false otherwise
	 * 
	 * precondition: the renter has rented the specified listing at least once in the past
	 */
	private static boolean reservationIsComplete(String renterSIN, String listingAddress, String listingPostalCode)
	{
		try
		{
			String condition = String.format("listingAddress='%s' AND listingPostalCode='%s' AND renterSIN=%s AND "
					+ "checkOut<=CURDATE()",
					listingAddress, listingPostalCode, renterSIN);
			ResultSet res = executeOperation(Ops.RESERVATION_FIND.toString() + condition);
			if(res.next())
			{
				return true;
			}
			System.out.println("You can add your review on this listing once the reservation is over");
			return false;
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * Finds the earliest reservation made by renter with <renterSIN> on one of the listings hosted by host with <hostSIN>
	 * Returns true if that reservation has already passed and false otherwise
	 * 
	 * precondition: the renter has rented the specified listing at least once in the past
	 */
	private static boolean reservationIsComplete(String hostSIN, String renterSIN)
	{
		try
		{
			String condition = String.format("R.renterSIN=%s AND L.host=%s AND R.checkout<=CURDATE() "
					+ "ORDER BY checkOut",
					renterSIN, hostSIN);
			ResultSet res = executeOperation(Ops.RESERVATION_HOST_FIND + condition);
			
			if(res.next())
			{
				return true;
			}
			System.out.println("You cann add your review on this renter once the reservation is over");
			return false;
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * Stores the comment and rating of host with <hostSIN> on the renter with <renterSIN>
	 * returns 1 if the review was successfully uploaded and 0 otherwise
	 */
	public static int hostReviewOnRenter(String hostSIN, String renterSIN, String comment, String rating)
	{
		// check that the host making this comment has rented their listing to the renter they are commenting on
		// and that the reservation is already over
		if(isHostOfReservation(hostSIN, renterSIN) && reservationIsComplete(hostSIN, renterSIN))
		{
			System.out.println(Ops.RENTER_REVIEW_CREATE.toString() +
					String.format("(%s, %s, '%s', %s)", hostSIN, renterSIN, comment, rating));
			return executeModification(Ops.RENTER_REVIEW_CREATE.toString() +
					String.format("(%s, %s, '%s', %s)", hostSIN, renterSIN, comment, rating));
		}
		System.out.println("Request denied: You are either not the host of this listing or this person did not rent from you");
		return 0;
	}
	
	/*
	 * Checks if there is at least one reservation where the host with <hostSIN>
	 * is the host of the renter with <renterSIN>
	 * returns true if the host hosted the renter at least once and false otherwise
	 */
	private static boolean isHostOfReservation(String hostSIN, String renterSIN)
	{
		try
		{
			String condition = String.format("R.renterSIN=%s AND L.host=%s", renterSIN, hostSIN);
			ResultSet res = executeOperation(Ops.RESERVATION_HOST_FIND.toString() + condition);
			
			if(res.next())
			{
				return true;
			}
			System.out.println("Must be the host of this reservation and the reservation must be over");
			return false;
		}
		catch(SQLException e)
		{
			System.out.println("Request failed");
			return false;
		}
	}
	
	/*
	 * Updates the price of listing with <address> and <postalCode> to <newPrice>
	 * Return 1 on success and 0 on failure
	 */
	public static int updateListingPrice(String address, String postalCode, String newPrice, String user)
	{
		if(userIsListingHost(user,address, postalCode))
		{
			String condition = String.format("address='%s' AND postalCode='%s'", address, postalCode);
			String query = Ops.LISTING_UPDATE + "pricePerNight=" + newPrice + " WHERE " + condition;
			return executeModification(query);
		}
		System.out.println("Request denied: you are not this listing's host");
		return 0;
	}
	
	public static String getUserFromUsername(String username)
	{
		ResultSet res = executeOperation("SELECT SIN FROM User WHERE username='"+username+"'");
		try
		{
			if(res.next())
			{
				return res.getString("SIN");
			}
			System.out.println("User not found");
			return null;
		}
		catch(SQLException e)
		{
			System.out.println("User not found");
			return null;
		}
	}
}
