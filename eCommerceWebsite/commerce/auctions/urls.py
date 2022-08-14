from django.urls import path

from . import views

from django.contrib.staticfiles.urls import staticfiles_urlpatterns

urlpatterns = [
    path("", views.index, name="index"),
    path("login", views.login_view, name="login"),
    path("logout", views.logout_view, name="logout"),
    path("register", views.register, name="register"),
    path("create_listing", views.create_listing, name="create"),
    path("<int:item_id>", views.auction_item, name="item"),
    path("<int:item_id>/place_bid", views.place_bid, name="bid"),
    path("watchlist", views.watchlist, name="watchlist"),
    path("closed_listings", views.closed_listings, name="closed"),
    path("categories", views.categories, name="categories"),
    path("<str:category_search>", views.cat_search, name="category_search")
]

urlpatterns += staticfiles_urlpatterns()
