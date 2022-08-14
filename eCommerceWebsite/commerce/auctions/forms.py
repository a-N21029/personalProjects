from django import forms

CATEGORIES = (
    ("Food", "Food"),
    ("Games", "Games"),
    ("Beauty", "Beauty"),
    ("Fitness", "Fitness"),
    ("Electronics", "Electronics"),
    ("Shopping", "Shopping")
)


class CategoriesForm(forms.Form):
    categories = forms.MultipleChoiceField(choices=CATEGORIES, required=False)
