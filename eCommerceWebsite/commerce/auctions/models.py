from django.contrib.auth.models import AbstractUser
from django.db import models
from datetime import timedelta


class User(AbstractUser):
    pass


class AuctionListing(models.Model):
    """
    A model representing all the available auctions listed

    === FIELDS ===

    item_name: the name of the item being auctioned
    item_description: a short description of what the item is/ what it does
    starting_price: the starting price of the item
    current_price: the current price of the item
    user: the user which placed the item on the website for auctioning
    pic: a picture of the item being auctioned
    create_timestamp: a timestamp for when the listing is created
    update_timestamp: a timestamp for each time the listing's information is
    updated
    bids: all the bids being placed on the item
    deadline: the last moment someone can place a bid
    categories: the categories of the model
    """
    item_name = models.CharField(max_length=32)
    item_description = models.CharField(max_length=64)
    # To make sure no one can input a negative price, the starting price must be
    # at least zero
    starting_price = models.FloatField()
    current_price = models.FloatField()
    # if a user were to be deleted, the listings they made will also get deleted
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    pic = models.ImageField(upload_to="media")
    # auto_now_add saves a timestamp when the object is created but not updated
    # auto_now saves a timestamp for each time the object is updated
    create_timestamp = models.DateTimeField(auto_now_add=True, auto_now=False)
    update_timestamp = models.DateTimeField(auto_now_add=False, auto_now=True)
    deadline = models.DateTimeField()
    # although categories is a CharField, a list will be stored in it in the
    # form of a string and will be casted back into a list when the data needs
    # to be extracted
    categories = models.CharField(max_length=128, blank=True, null=True)
    watchlist_users = models.ManyToManyField(User, blank=True,
                                             related_name="watchlist")
    auction_winner = models.ForeignKey(User, on_delete=models.DO_NOTHING,
                                       blank=True, null=True,
                                       related_name="winner")
    is_closed = models.BooleanField(default=False)

    def categs(self):
        """
        Converts the categories stored in self.categories to a list
        """
        # the range is from 1 to -1 to exclude  the "[]" square brackets
        lst = f'{self.categories}'[1:-1].split(",")
        if "" in lst:
            lst.remove("")
        return lst

    def time_adjust(self):
        """
        The use of the django timezone module will be used in the application
        and this creates time difference of four hours from real time in
        whichever timezone the user is present and so this method adjusts that
        time difference
        """
        self.create_timestamp -= timedelta(hours=4)
        self.save()


class AuctionBid(models.Model):
    """
    A model representing a bid being made for a model. To be used as a foreign
    key in the AuctionListing class

    === FIELDS ===

    user: the user which is making the bid
    bid_amount: the amount the user is bidding
    """
    # if a user were to be deleted, the bids they made will also get deleted
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    bid_amount = models.FloatField()
    listing = models.ForeignKey(AuctionListing, on_delete=models.CASCADE,
                                blank=True, null=True)


class AuctionComment(models.Model):
    """
    A model representing the comments that were made on the page of an auction
    listing.

    === FIELDS ==
    user: the user who made the comment
    comment: the comment itself
    timestamp: a timestamp for when the comment was uploaded to the website
    listing: the listing whose page the comment was made on
    """
    # if a user were to be deleted, their comments will also get deleted
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    comment = models.CharField(max_length=256)
    timestamp = models.DateField(auto_now_add=True)
    listing = models.ForeignKey(AuctionListing, on_delete=models.CASCADE)
