# Generated by Django 3.2.3 on 2021-07-09 00:54

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('auctions', '0007_auctionbid'),
    ]

    operations = [
        migrations.AddField(
            model_name='auctionlisting',
            name='bids',
            field=models.ForeignKey(blank=True, null=True, on_delete=django.db.models.deletion.DO_NOTHING, to='auctions.auctionbid'),
        ),
    ]
