# Generated by Django 3.2.3 on 2021-07-10 22:08

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('auctions', '0012_alter_auctionlisting_bids'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='auctionlisting',
            name='bids',
        ),
        migrations.AddField(
            model_name='auctionlisting',
            name='bids',
            field=models.ManyToManyField(blank=True, null=True, to='auctions.AuctionBid'),
        ),
    ]