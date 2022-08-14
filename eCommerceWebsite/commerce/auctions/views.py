from django.contrib.auth import authenticate, login, logout
from django.db import IntegrityError
from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render
from django.urls import reverse
from .forms import CategoriesForm, CATEGORIES
from django.utils import timezone
from datetime import timedelta

from .models import User, AuctionListing, AuctionBid, AuctionComment


def index(request):
    if request.method == "POST":
        listing_id = int(request.POST["listing"])
        if request.POST["form_type"] == "visit":
            return HttpResponseRedirect(reverse("item", args=(listing_id,)))
        elif request.POST["form_type"] == "bid":
            return HttpResponseRedirect(reverse("bid", args=(listing_id,)))

    else:
        listings = AuctionListing.objects.filter(is_closed=False)
        closed = AuctionListing.objects.filter(is_closed=True)
        if request.user.is_authenticated:
            user_id = request.user.id
            user = User.objects.get(pk=user_id)
            context = {
                "listings": AuctionListing.objects.exclude(is_closed=True),
                "num_listings": listings.count(),
                "num_watchlist": user.watchlist.count(),
                "num_closed": closed.count()
            }
        else:
            context = {
                "listings": AuctionListing.objects.all(),
                "num_listings": listings.count()
            }
        return render(request, "auctions/index.html", context)


def login_view(request):
    if request.method == "POST":

        # Attempt to sign user in
        username = request.POST["username"]
        password = request.POST["password"]
        user = authenticate(request, username=username, password=password)

        # Check if authentication successful
        if user is not None:
            login(request, user)
            return HttpResponseRedirect(reverse("index"))
        else:
            return render(request, "auctions/login.html", {
                "message": "Invalid username and/or password."
            })
    else:
        return render(request, "auctions/login.html")


def logout_view(request):
    logout(request)
    return HttpResponseRedirect(reverse("index"))


def register(request):
    if request.method == "POST":
        username = request.POST["username"]
        email = request.POST["email"]

        # Ensure password matches confirmation
        password = request.POST["password"]
        confirmation = request.POST["confirmation"]
        if password != confirmation:
            return render(request, "auctions/register.html", {
                "message": "Passwords must match."
            })

        # Attempt to create new user
        try:
            user = User.objects.create_user(username, email, password)
            user.save()
        except IntegrityError:
            return render(request, "auctions/register.html", {
                "message": "Username already taken."
            })
        login(request, user)
        return HttpResponseRedirect(reverse("index"))
    else:
        return render(request, "auctions/register.html")


def create_listing(request):
    # the form used for categories
    form = CategoriesForm(request.POST)

    if request.method == "POST":
        item_name = request.POST["IN"]
        item_description = request.POST["ID"]
        starting_price = request.POST["SP"]
        user_id = int(request.POST["uid"])
        user = User.objects.get(pk=user_id)
        pic = request.POST["pic"]
        deadline = request.POST["date"]

        # make sure that the price is always rounded to 2 decimal places
        starting_price = round(float(starting_price), 2)

        if form.is_valid():
            # turns the multiple selected categories into a python list
            categories = form.cleaned_data.get("categories")

            # create the listing object
            listing = AuctionListing.objects.create(item_name=item_name,
                                                    item_description=
                                                    item_description,
                                                    starting_price=
                                                    starting_price,
                                                    current_price=
                                                    starting_price,
                                                    user=user, pic=pic,
                                                    deadline=deadline,
                                                    categories=categories)
            listing.save()
            listing.time_adjust()
            listing.save()
            return HttpResponseRedirect(reverse("index"))

    else:
        return render(request, "auctions/create.html", {
            "form": form
        })


def auction_item(request, item_id):
    listing = AuctionListing.objects.get(pk=item_id)
    num_bids = AuctionBid.objects.filter(listing=listing).count()

    # adjust the device time to the time we want for comparison
    adjust = timedelta(hours=4)
    if timezone.now() > (listing.deadline + adjust) and not \
            listing.is_closed:
        if num_bids > 0:
            highest_bid = AuctionBid.objects.get(listing=listing,
                                                 bid_amount=
                                                 listing.current_price)
            highest_bid_user = highest_bid.user
            listing.auction_winner = highest_bid_user
            listing.is_closed = True
            listing.save()
        else:
            listing.delete()
            return HttpResponseRedirect(reverse('index'))
    try:  # this will not work if no bids have been made yet
        highest_bid = AuctionBid.objects.get(listing=listing,
                                             bid_amount=listing.current_price)
    except AuctionBid.DoesNotExist:
        # it will be set to None so as to not raise any errors
        highest_bid = None
    comments = AuctionComment.objects.filter(listing=listing)
    num_comments = comments.count()

    if request.method == "POST":
        if request.POST["form_type"] == "bid":
            return HttpResponseRedirect(reverse("bid", args=(item_id,)))
        elif request.POST["form_type"] == "comment":
            make_comment(request, listing)
            return HttpResponseRedirect(reverse("item", args=(item_id,)))
        elif request.POST["form_type"] == "watchlist":
            user_id = int(request.POST["uid"])
            user = User.objects.get(pk=user_id)
            listing.watchlist_users.add(user)
            return HttpResponseRedirect(reverse("watchlist"))
        elif request.POST["form_type"] == "close":
            return close_listing(listing)
    else:
        user = User.objects.get(pk=request.user.id)
        context = {
            "listing": listing,
            "num_bids": num_bids,
            "comments": comments,
            "num_comments": num_comments,
            # this will be used when deciding whether to render the button
            # "remove from watchlist' or 'add to watchlist'
            "watchlisted": listing in user.watchlist.all()
        }

        if highest_bid:
            context["bid_user"] = highest_bid.user
        else:
            context["bid_user"] = highest_bid

        return render(request, "auctions/item.html", context)


def place_bid(request, item_id):
    # this boolean will be used when choosing what to render in the bid
    # placing html page
    bid_is_valid = True
    if request.method == "POST":
        # the user making the bid
        user_id = int(request.POST["uid"])
        user = User.objects.get(pk=user_id)
        # the current highest bid of the auction listing
        current_price = round(float(request.POST["CP"]), 2)
        bid_amount = round(float(request.POST["BA"]), 2)

        # the bid will only be considered valid if it is strictly greater than
        # the current highest price
        if bid_amount > current_price:
            l = AuctionListing.objects.get(pk=item_id)
            # store bid information inside the AuctionBid model
            fields = {
                "item_name": l.item_name,
                "item_description": l.item_description,
                "starting_price": l.starting_price,
                "current_price": bid_amount,  # THE CURRENT PRICE CHANGES
                "user": l.user,
                "pic": l.pic,
                "create_timestamp": l.create_timestamp,
                "update_timestamp": l.update_timestamp,
                "deadline": l.deadline
            }
            l.__dict__.update(fields)
            l.save()
            bid = AuctionBid.objects.create(user=user, bid_amount=bid_amount,
                                            listing=l)
            bid.save()
            return HttpResponseRedirect(reverse("item", args=(item_id,)))
        else:
            bid_is_valid = False
            return render(request, "auctions/bid.html", {
                "bid_is_valid": bid_is_valid,
                "listing": AuctionListing.objects.get(pk=item_id)
            })
    else:
        return render(request, "auctions/bid.html", {
            "bid_is_valid": bid_is_valid,
            "listing": AuctionListing.objects.get(pk=item_id)
        })


# This function is not associated with a path unlike most of the other functions
# It is to be used within the function for rendering the page of a listing
def make_comment(request, listing):
    user_id = int(request.POST["uid"])
    user = User.objects.get(pk=user_id)
    comment = request.POST["comment"]
    item = listing

    c = AuctionComment.objects.create(user=user, comment=comment, listing=item)
    c.save()


def close_listing(listing):
    highest_bid = AuctionBid.objects.get(listing=listing,
                                         bid_amount=listing.current_price)
    winner = highest_bid.user
    listing.is_closed = True
    listing.auction_winner = winner
    listing.save()
    return HttpResponseRedirect(reverse("index"))


def watchlist(request):
    user_id = request.user.id
    user = User.objects.get(pk=user_id)
    watchlistings = user.watchlist.all()

    if request.method == "POST":
        listing_id = int(request.POST["listing"])
        listing = AuctionListing.objects.get(pk=listing_id)
        if request.POST["form_type"] == "remove":
            listing.watchlist_users.remove(user)
            return HttpResponseRedirect(reverse("watchlist"))
    else:
        return render(request, "auctions/watchlist.html", {
            "signed_user": user,
            "watchlist": watchlistings
        })


def closed_listings(request):
    listings = AuctionListing.objects.filter(is_closed=True)
    return render(request, "auctions/closed.html", {
        "listings": listings
    })


def categories(request):
    categs = [category[0] for category in CATEGORIES]
    return render(request, "auctions/categories.html", {
        "categories": categs
    })


def cat_search(request, category_search):
    # uses list comprehension to filter the auction listing items by the ones
    # that have the desired category in their list of categories
    listings = [listing for listing in AuctionListing.objects.filter(
        is_closed=False) if category_search in listing.categories]
    return render(request, "auctions/catSearch.html", {
        "listings": listings,
    })
