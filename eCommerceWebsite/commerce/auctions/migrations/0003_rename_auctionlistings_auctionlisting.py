# Generated by Django 3.2.3 on 2021-07-05 23:50

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('auctions', '0002_auctionlistings'),
    ]

    operations = [
        migrations.RenameModel(
            old_name='AuctionListings',
            new_name='AuctionListing',
        ),
    ]