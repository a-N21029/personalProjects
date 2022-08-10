package airBnb;


import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.HashMap;

public class Main {
	
	private static String currentUser;
	private static boolean isHost, isRenter, logout;
	
	// scanner responsible for taking user input
	private static Scanner prompt = new Scanner(System.in);
	
	public static void main(String[] args) {
		while(greetUser() == 0)
		{
			
		}
		logout = false;
		
		while(!logout)
		{
			showOptions();
		}
		System.out.println("Bye!");
		prompt.close();
	}
		
		
	
	
	/*
	 * Greets user and gives them the option to sign in or register
	 * Returns 1 on successful sign in, 2 on successful register and
	 * 0 on a failed login or register
	 */
	private static int greetUser()
	{
		System.out.println("Hi! Would you like to:\n1. Register\n2. Sign in");
		String input = prompt.nextLine();
		System.out.print("SIN: ");
		String sin = prompt.nextLine();
		System.out.print("Password (press enter for no password): ");
		String password = prompt.nextLine();
		
		if(input.equals("1") || input.toLowerCase().equals("register"))
		{
			System.out.print("First Name: ");
			String fn = prompt.nextLine();
			System.out.print("last Name: ");
			String ln = prompt.nextLine();
			System.out.print("Username: ");
			String username = prompt.nextLine();
			System.out.print("Date of birth(YYYY-MM-DD): ");
			String dob = prompt.nextLine();
			System.out.print("Address: ");
			String addr = prompt.nextLine();
			System.out.print("Occupation: ");
			String occ = prompt.nextLine();
			System.out.print("Would you like to be host(Y/N): ");
			isHost = prompt.nextLine().toLowerCase().equals("y");
			System.out.print("Would you like to be renter(Y/N): ");
			isRenter = prompt.nextLine().toLowerCase().equals("y");
			if(Operations.createUser(sin, fn, ln, username, dob, addr, occ, password, isHost, isRenter) == 1)
			{
				currentUser = sin;
				System.out.println("Registration Successfull!");
				return 2;
			}
			System.out.println("User already exists");
			return 0;
		}
		else if (input.equals("2") || input.toLowerCase().equals("sign in"))
		{
			int login = Operations.login(sin, password);
			if(login > 0)
			{
				currentUser = Integer.toString(login);
				System.out.println("Welcome back " + Operations.getUserName(currentUser) + "!");
				return 1;
			}
			else if(login == -1)
			{
				System.out.println(String.format("User with SIN %s does not exist", sin));
			}
			else
			{
				System.out.println("login failed");
			}
		}
		return 0;
	}
	
	private static int showOptions()
	{
		boolean isHost = Operations.isHost(currentUser);
		boolean isRenter = Operations.isRenter(currentUser);
		System.out.println("What would you like to do:");
		if(isHost)
		{
			System.out.println("- Create a Listing (CL)");
			System.out.println("- Remove a Listing (RL)");
			System.out.println("- Update listing price (UP)");
			System.out.println("- Create an unavailable time range for a Listing (CU)");
			System.out.println("- Remove an unavailable time range for a Listing (RU)");
			System.out.println("- Comment on past renter (RC)");
		}
		if(isRenter)
		{
			System.out.println("- Book a Reservation (BR)");
			System.out.println("- Comment on past listing (LC)");
			System.out.println("- Search for listings close to a specific location (SL)");
			System.out.println("- Search for listings close to a specific postal code (SP)");
			System.out.println("- Search for a listing with a specific address (SS)");
		}
		System.out.println("- Cancel a Reservation (CR)");
		System.out.println("- Run a report (RP)");
		System.out.println("- Switch account (SA)");
		System.out.println("- Logout (L)");
		System.out.println("- Delete account (DA)");
		
		String answer = prompt.nextLine().toLowerCase();
		
		int status = 1; 
		
		switch(answer)
		{
			case "cl":
				status = createListing();
				break;
			case "rl":
				status = removeListing();
				break;
			case "up":
				updatePrice();
				break;
			case "br":
				status = bookReservation();
				break;
			case "cr":
				status = cancelReservation();
				break;
			case "cu":
				status = createUnavailability();
				break;
			case "ru":
				status = removeUnavailability();
				break;
			case "rc":
				status = commentOnRenter();
				break;
			case "lc":
				status = commentOnListing();
				break;
			case "sa":
				status = greetUser();
				break;
			case "sl":
				status = searchNearbyListings();
				break;
			case "sp":
				status = searchListingsByPostalCode();
				break;
			case "ss":
				status = findListingByExactAddress();
				break;
			case "rp":
				status = showReports();
				break;
			case "da":
				Operations.deleteUser(currentUser);
			case "l":
				logout = true;
				
		}
		return status;
	}
	
	/*
	 * Creates a listing associated with the current user
	 * Returns 1 on success, 0 on failure and -1 if the user making this request is not a host
	 */
	private static int createListing()
	{	
		String ppn, amens;
		System.out.print("Address: ");
		String addr = prompt.nextLine();
		System.out.print("\nPostal Code: ");
		String pc = prompt.nextLine();
		System.out.print("\nCity: ");
		String city = prompt.nextLine();
		System.out.print("\nCountry: ");
		String country = prompt.nextLine();
		System.out.print("\nType of listing (house, apartment, guest house or hotel): ");
		String type = prompt.nextLine();
		System.out.print("\nLatitude: ");
		String lat = prompt.nextLine();
		System.out.print("\nLongitude: ");
		String lng = prompt.nextLine();
		
		System.out.println("Would you like the host toolkit to help you with pricing and amenities? (Y/N): ");
		boolean htk = prompt.nextLine().toLowerCase().equals("y");
		
		if(htk)
		{
			HostToolKit h = new HostToolKit(city, country);
			ppn = h.getPriceSuggestion();
			amens = h.getAmenitySuggestions();
			System.out.println("The price will be set to: " + h.getPriceSuggestion());
			System.out.println("The amenities will be set to: " + h.getAmenitySuggestions()); 
			System.out.println("You can always change them later");
		}
		else
		{
			System.out.print("\nPrice per night: ");
			ppn = prompt.nextLine();
			
			showAmenities();
			System.out.println("\nWhich amenities do you offer from the ones listed above? Separate values by commas NO SPACES(e.x. 'TV,Wifi'): ");
			amens = prompt.nextLine();
		}
		return Operations.createListing(addr, pc, city, country, type, lat, lng, ppn, currentUser, amens);
	}
	
	private static void showAmenities()
	{
		String [] amenities = Operations.getAmenities();
		int i = 0;
		while(i < amenities.length && amenities[i] != null)
		{
			System.out.println("- " + amenities[i]);
			i++;
		}
	}
	
	/*
	 * Removes a listing associated with the current user as the host
	 * Returns 1 on success, 0 on failure and -1 if the user making this request is not a host
	 */
	private static int removeListing()
	{
		System.out.print("Address: ");
		String addr = prompt.nextLine();
		System.out.print("\nPostal Code: ");
		String pc = prompt.nextLine();
		return Operations.removeListing(addr, pc, currentUser);
	}
	
	/*
	 * Updates the price of a listing associated with the current user
	 */
	private static int updatePrice()
	{
		System.out.print("Address: ");
		String addr = prompt.nextLine();
		System.out.print("\nPostal Code: ");
		String pc = prompt.nextLine();
		System.out.println("What would you like the new price to be: ");
		String curPrice = prompt.nextLine();
		return Operations.updateListingPrice(addr, pc, curPrice, currentUser);
	}
	
	private static int createUnavailability()
	{
		System.out.print("Address: ");
		String addr = prompt.nextLine();
		System.out.print("\nPostal Code: ");
		String pc = prompt.nextLine();
		System.out.print("Start date: ");
		String sd = prompt.nextLine();
		System.out.print("\nEnd date: ");
		String ed = prompt.nextLine();
		return Operations.createUnavailability(addr, pc, sd, ed, currentUser);
	}
	
	private static int removeUnavailability()
	{
		System.out.print("Address: ");
		String addr = prompt.nextLine();
		System.out.print("\nPostal Code: ");
		String pc = prompt.nextLine();
		System.out.print("Start date: ");
		String sd = prompt.nextLine();
		System.out.print("\nEnd date: ");
		String ed = prompt.nextLine();
		return Operations.deleteUnavailability(addr, pc, sd, ed, currentUser);
	}
	
	private static int commentOnRenter()
	{
		System.out.println("Username of renter you would like to comment on: ");
		String renter = prompt.nextLine();
		System.out.println("Rating (out of 5): ");
		String rating = prompt.nextLine();
		System.out.println("Your review on the renter (MAX 255 CHARACTERS): ");
		String comment = prompt.nextLine();
		String renterSIN = Operations.getUserFromUsername(renter);
		return Operations.hostReviewOnRenter(currentUser, renterSIN, comment, rating);
	}
	
	/*
	 * Books a reservation with the current user as the renter
	 */
	private static int bookReservation()
	{
		System.out.println("Payment information (Format: CARD_NUMBER-CVV-EXPIRATION_DATE_MMYY): ");
		String payment = prompt.nextLine();
		System.out.print("\nCheck in (YYYY-MM-DD): ");
		String ci = prompt.nextLine();
		System.out.print("\nCheck out (YYYY-MM-DD): ");
		String co = prompt.nextLine();
		System.out.print("Address: ");
		String addr = prompt.nextLine();
		System.out.print("\nPostal Code: ");
		String pc = prompt.nextLine();
		return Operations.bookReservation(currentUser, payment, ci, co, addr, pc);
	}
	
	private static int commentOnListing()
	{
		System.out.println("Address of listing you would like to comment on: ");
		String addr = prompt.nextLine();
		System.out.println("Postal code of listing you would like to comment on: ");
		String pc = prompt.nextLine();
		System.out.println("Rating (out of 5): ");
		String rating = prompt.nextLine();
		System.out.println("Your review on the renter (MAX 255 CHARACTERS): ");
		String comment = prompt.nextLine();
		return Operations.renterReviewOnListing(currentUser, addr, pc, comment, rating);
	}

	private static int searchNearbyListings()
	{
		String sd, ed, lp, hp;
		sd = null;
		ed = null;
		lp = null;
		hp = null;
		String[] amenities = null;
		System.out.println("Latitude of location you would like to search near (latitude can be between -90 and 90, inclusive): ");
		String lat = prompt.nextLine();
		System.out.println("Longitude of location you would like to search near (longitude can be between -120 and 120, inclusive): ");
		String lng = prompt.nextLine();
		System.out.println("How far, in km, would you like the search to extend from that location: ");
		String vicinity = prompt.nextLine();
		System.out.println("Would you like the results to be showed in order of price, or distance? (P/D): ");
		boolean sortByDistance = prompt.nextLine().toLowerCase().equals("d");
		System.out.println("Would you like the results to be showed in ascending, or descending order? (A/D): ");
		boolean sortAscending = prompt.nextLine().toLowerCase().equals("a");
		System.out.println("Would you like to add a temporal filter (Y/N): ");
		if(prompt.nextLine().toLowerCase().equals("y"))
		{
			System.out.println("Earliest date (YYYY-MM-DD): ");
			sd = prompt.nextLine();
			System.out.println("Latest date (YYYY-MM-DD): ");
			ed = prompt.nextLine();
		}
		System.out.println("Would you like to add a price filter (Y/N): ");
		if(prompt.nextLine().toLowerCase().equals("y"))
		{
			System.out.println("Lowest price: ");
			lp = prompt.nextLine();
			System.out.println("Highest price: ");
			hp = prompt.nextLine();
		}
		System.out.println("Would you like to add an amenity filter (Y/N): ");
		if(prompt.nextLine().toLowerCase().equals("y"))
		{
			showAmenities();
			System.out.println("Which amenities do you want a a listing to offer from the ones listed above? Separate values by commas NO SPACES(e.x. 'TV,Wifi'): ");
			amenities = prompt.nextLine().split(",");
		}
		ResultSet res = Queries.findListingsInVicinity(lat, lng, vicinity, sortByDistance, sortAscending, "0", sd, ed, lp, hp, amenities);
		int status = showResults(res);
		
		System.out.println("Show more results? (Y/N): ");
		boolean showMore = prompt.nextLine().toLowerCase().equals("y");
		
		if(showMore)
		{
			int offset = 10;
			while(showMore && status == 1)
			{
				for (int k = 0; k < 20; k++) {
					System.out.print("-");
				}
				System.out.println();
				res = Queries.findListingsInVicinity(lat, lng, vicinity, sortByDistance, sortAscending,
						Integer.toString(offset), sd, ed, lp, hp, amenities);
				status *= showResults(res);
				if(status == 1)
				{
					System.out.println("Show more results? (Y/N): ");
					showMore = prompt.nextLine().toLowerCase().equals("y");
				}
				offset += 10;
			}
		}
		return status;
	}
	
	private static int searchListingsByPostalCode()
	{
		String sd, ed, lp, hp;
		sd = null;
		ed = null;
		lp = null;
		hp = null;
		String[] amenities = null;
		boolean sortAscending = false;
		
		System.out.println("Postal Code: ");
		String pc = prompt.nextLine();
		System.out.println("Would you like the results to be ordered by price? (Y/N): ");
		boolean sortByPrice= prompt.nextLine().toLowerCase().equals("y");
		if(sortByPrice)
		{
			System.out.println("Would you like the results to be showed in ascending, or descending order? (A/D): ");
			sortAscending = prompt.nextLine().toLowerCase().equals("a");	
		}
		System.out.println("Would you like to add a temporal filter (Y/N): ");
		if(prompt.nextLine().toLowerCase().equals("y"))
		{
			System.out.println("Earliest date (YYYY-MM-DD): ");
			sd = prompt.nextLine();
			System.out.println("Latest date (YYYY-MM-DD): ");
			ed = prompt.nextLine();
		}
		System.out.println("Would you like to add a price filter (Y/N): ");
		if(prompt.nextLine().toLowerCase().equals("y"))
		{
			System.out.println("Lowest price: ");
			lp = prompt.nextLine();
			System.out.println("Highest price: ");
			hp = prompt.nextLine();
		}
		System.out.println("Would you like to add an amenity filter (Y/N): ");
		if(prompt.nextLine().toLowerCase().equals("y"))
		{
			showAmenities();
			System.out.println("Which amenities do you want a a listing to offer from the ones listed above? Separate values by commas NO SPACES(e.x. 'TV,Wifi'): ");
			amenities = prompt.nextLine().split(",");
		}
		ResultSet res = Queries.findListingsByPostalCode(pc, sortByPrice, sortAscending, "0", sd, ed, lp, hp, amenities);
		int status = showResults(res);
		
		System.out.println("Show more results? (Y/N): ");
		boolean showMore = prompt.nextLine().toLowerCase().equals("y");
		
		if(showMore)
		{
			int offset = 10;
			while(showMore && status == 1)
			{
				for (int k = 0; k < 20; k++) {
					System.out.print("-");
				}
				System.out.println();
				res = Queries.findListingsByPostalCode(pc, sortByPrice, sortAscending,
						Integer.toString(offset), sd, ed, lp, hp, amenities);
				status *= showResults(res);
				if(status == 1)
				{
					System.out.println("Show more results? (Y/N): ");
					showMore = prompt.nextLine().toLowerCase().equals("y");
				}
				offset += 10;
			}
		}
		return status;
		
	}
	
	private static int findListingByExactAddress()
	{
		String sd, ed, lp, hp;
		sd = null;
		ed = null;
		lp = null;
		hp = null;
		String[] amenities = null;
		
		System.out.println("Address: ");
		String addr = prompt.nextLine();
		System.out.println("Would you like to add a temporal filter (Y/N): ");
		if(prompt.nextLine().toLowerCase().equals("y"))
		{
			System.out.println("Earliest date (YYYY-MM-DD): ");
			sd = prompt.nextLine();
			System.out.println("Latest date (YYYY-MM-DD): ");
			ed = prompt.nextLine();
		}
		System.out.println("Would you like to add a price filter (Y/N): ");
		if(prompt.nextLine().toLowerCase().equals("y"))
		{
			System.out.println("Lowest price: ");
			lp = prompt.nextLine();
			System.out.println("Highest price: ");
			hp = prompt.nextLine();
		}
		System.out.println("Would you like to add an amenity filter (Y/N): ");
		if(prompt.nextLine().toLowerCase().equals("y"))
		{
			showAmenities();
			System.out.println("Which amenities do you want a a listing to offer from the ones listed above? Separate values by commas NO SPACES(e.x. 'TV,Wifi'): ");
			amenities = prompt.nextLine().split(",");
		}
		ResultSet res = Queries.searchByExactAddress(addr, sd, ed, lp, hp, amenities);
		int status = showResults(res);
		return status;
	}
	/*
	 * Cancels a reservation associated
	 * Returns 1 on success, 0 on failure
	 */
	private static int cancelReservation()
	{
		System.out.print("Address: ");
		String addr = prompt.nextLine();
		System.out.print("\nPostal Code: ");
		String pc = prompt.nextLine();
		System.out.print("\nCheck in (YYYY-MM-DD): ");
		String ci = prompt.nextLine();
		System.out.print("\nCheck out (YYYY-MM-DD): ");
		String co = prompt.nextLine();
		return Operations.cancelReservation(currentUser, addr, pc, ci, co);
	}
	
	private static int showReports()
	{
		System.out.println("Which report would you like to run: ");
		System.out.println("- Total number of bookings in a date range by city (TB)");
		System.out.println("- Total number of bookings in a date range by postal code within a city (PC)");
		System.out.println("- Total number of listing per country/per country and city/ per country and city and postal code (TC)");
		System.out.println("- Rank hosts by total number of listings per country/ per country and city(TL)");
		System.out.println("- Rank renters by total number of bookings in a time period (RR)");
		System.out.println("- Rank renters by total number of bookings in a time period per city(RP)");
		System.out.println("- Find hosts with more than 10% of listings per city and country(MT)");
		System.out.println("- Find hosts with largest number of cancellations in a year (HC)");
		System.out.println("- Find renters with largest number of cancellations in a year (RC)");
		System.out.println("- Show most common noun phrase associated with a liting (NP)");
		
		String answer = prompt.nextLine().toLowerCase();
		
		switch(answer)
		{
			case "tb":
				return totalBookingDateRange();
			case "pc":
				return totalBookingDateRangeCityAndPostalCode();
			case "tc":
				return totalListings();
			case "tl":
				return rankHostsByListingsPerCountry();
			case "rr":
				return rankRentersByBookingsInTimeRange();
			case "rp":
				return rankRentersByBookingsInTimeRangeAndCity();
			case "mt":
				return findMonopolizingHosts();
			case "hc":
				return findCancelledHosts();
			case "rc":
				return findCancelledRenters();
			case "np":
				showListingMostCommonPhrases(Reports.listingCommonNounPhrases());
		}
		return 0;
	}
	
	private static int totalBookingDateRange()
	{
		System.out.print("Start date (YYYY-MM-DD): ");
		String sd = prompt.nextLine();
		System.out.print("End date (YYYY-MM-DD): ");
		String ed = prompt.nextLine();
		
		ResultSet res = Reports.bookingsInDateRangeByCity(sd, ed);
		return showResults(res);
	}
	
	private static int totalBookingDateRangeCityAndPostalCode()
	{
		System.out.print("Start date (YYYY-MM-DD): ");
		String sd = prompt.nextLine();
		System.out.print("End date (YYYY-MM-DD): ");
		String ed = prompt.nextLine();
		
		ResultSet res = Reports.bookingsInDateRangeByCityAndPostalCode(sd, ed);
		return showResults(res);
	}
	
	
	private static int totalListings()
	{
		boolean pc = false;
		System.out.print("Would you like to refine the search to per country and city (Y/N): ");
		boolean city = prompt.nextLine().toLowerCase().equals("y");
		if(city)
		{
			System.out.print("Would you like to refine the search to per country and city and postal code (Y/N): : ");
			pc = prompt.nextLine().toLowerCase().equals("y");
		}
		ResultSet res = Reports.totalListings(city, pc);
		return showResults(res);
	}
	private static int rankHostsByListingsPerCountry()
	{
		System.out.print("Would you like to refine the search to per country and city (Y/N): ");
		boolean city = prompt.nextLine().toLowerCase().equals("y");
		
		ResultSet res = Reports.rankHostsByTotalListingsInCountry(city);
		return showResults(res);
	}
	
	private static int rankRentersByBookingsInTimeRange()
	{
		System.out.print("Start date (YYYY-MM-DD): ");
		String sd = prompt.nextLine();
		System.out.print("End date (YYYY-MM-DD): ");
		String ed = prompt.nextLine();
		
		ResultSet res = Reports.rankRentersByBookingInTimeRange(sd, ed);
		return showResults(res);
	}
	
	private static int rankRentersByBookingsInTimeRangeAndCity()
	{
		System.out.print("Start date (YYYY-MM-DD): ");
		String sd = prompt.nextLine();
		System.out.print("End date (YYYY-MM-DD): ");
		String ed = prompt.nextLine();
		
		ResultSet res = Reports.rankRentersByBookingInTimeRangeAndCity(sd, ed);
		return showResults(res);
	}
	
	private static int findMonopolizingHosts()
	{
		ResultSet res = Reports.findMonopolizingHosts();
		return showResults(res);
	}
	
	private static int findCancelledHosts()
	{
		ResultSet res = Reports.rankHostsByCancellation();
		return showResults(res);
	}
	
	private static int findCancelledRenters()
	{
		ResultSet res = Reports.rankRentersByCancellation();
		return showResults(res);
	}
	
	/*
	 * Returns 1 on success and 0 on failure
	 */
	private static int showResults(ResultSet res)
	{
		try
		{
			ResultSetMetaData r = res.getMetaData();
			int count = r.getColumnCount();
			
			while(res.next()) {
				for (int k = 0; k < 20; k++) {
					System.out.print("-");
				}
				System.out.println("");
				for(int i = 1; i < count + 1; i++) {
					System.out.println(r.getColumnName(i) + ": " + res.getString(i));
				}
			}
			System.out.println();
			System.out.println("End of results reached.");
			System.out.println();
			return 1;
		}
		catch(SQLException e)
		{
			return 0;
		}
	}
	
	private static void showListingMostCommonPhrases(HashMap<String, String> h)
	{
		for(String i : h.keySet())
		{
			System.out.println(i + ": " + h.get(i));
		}
	}
	
	
	
	
}
