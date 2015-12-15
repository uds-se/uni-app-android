package de.unisaarland.UniApp.restaurant.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;

import java.io.Serializable;

public class MensaItem implements Serializable, Parcelable {

    private final String category;
    private final String desc;
    private final String title;
    private final String[] labels;
    private final int preis1;
    private final int preis2;
    private final int preis3;
    private final int color;

    public MensaItem(String category, String desc, String title, String[] labels,
                     int preis1, int preis2, int preis3, int color) {
        this.category = category;
        this.desc = desc;
        this.title = title;
        this.labels = labels;
        this.preis1 = preis1;
        this.preis2 = preis2;
        this.preis3 = preis3;
        this.color = color;
    }

    public String getCategory() {
        return category;
    }

    public String getDesc() {
        return desc;
    }

    public String getTitle() {
        return title;
    }

    public String[] getLabels() {
        return labels;
    }

    public int getPreis1() {
        return preis1;
    }

    public int getPreis2() {
        return preis2;
    }

    public int getPreis3() {
        return preis3;
    }

    public int getColor() {
        return color;
    }

    private boolean isValidIngredients(String substring) {
        String[] parts = substring.split(",");
        for (String part : parts)
            if (part.trim().length() > 2)
                return false;
        return true;
    }

    /**
     * if showIngredients is set: returns a SpannableString for the given text. All occurences
     * of (A,B,C) are put in superscript with the parentheses removed.
     * otherwise: returns just a string with all those occurences removed.
     */
    private CharSequence createMensaItemSpannable(String desc, boolean showIngredients) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        for (int pos = 0, oldPos = 0; pos < desc.length(); oldPos = pos) {
            int openParen = desc.indexOf('(', oldPos);
            int closeParen = desc.indexOf(')', openParen+1);
            pos = closeParen+1;
            if (openParen == -1 || closeParen == -1) {
                sb.append(desc, oldPos, desc.length());
                break;
            }
            if (!isValidIngredients(desc.substring(openParen+1, closeParen))) {
                sb.append(desc, oldPos, pos);
                continue;
            }
            // remove the potential space before the ingredients
            int firstStrEnd = openParen;
            while (firstStrEnd > oldPos && desc.charAt(firstStrEnd - 1) == ' ')
                --firstStrEnd;
            sb.append(desc, oldPos, firstStrEnd);
            if (showIngredients) {
                int oldLen = sb.length();
                sb.append(desc, openParen + 1, closeParen);
                int newLen = sb.length();
                sb.setSpan(new SuperscriptSpan(), oldLen, newLen,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(0.8f), oldLen, newLen,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        if (!showIngredients)
            return sb.toString(); // no spannable string, just the normal string

        return sb;
    }

    /**
     * If showIngredients is set: returns a SpannableString for the title of this item with all
     * occurences of (A,B,C) put in superscript with the parentheses removed.
     * otherwise: returns just the title with the ingredients removed.
     * @param showIngredients
     */
    public CharSequence getTitleSpannable(boolean showIngredients) {
        return createMensaItemSpannable(getTitle(), showIngredients);
    }

    /**
     * If showIngredients is set: returns a SpannableString for the description of this item with
     * all occurences of (A,B,C) put in superscript with the parentheses removed.
     * otherwise: returns just the description with the ingredients removed.
     * @param showIngredients
     */
    public CharSequence getDescSpannable(boolean showIngredients) {
        return createMensaItemSpannable(getDesc(), showIngredients);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(desc);
        dest.writeString(title);
        dest.writeStringArray(labels);
        dest.writeInt(preis1);
        dest.writeInt(preis2);
        dest.writeInt(preis3);
        dest.writeInt(color);
    }

    public static final Parcelable.Creator<MensaItem> CREATOR
            = new Parcelable.Creator<MensaItem>() {
        public MensaItem createFromParcel(Parcel in) {
            return new MensaItem(
                    /* category */ in.readString(),
                    /* desc     */ in.readString(),
                    /* title    */ in.readString(),
                    /* labels   */ in.createStringArray(),
                    /* preis1   */ in.readInt(),
                    /* preis2   */ in.readInt(),
                    /* preis3   */ in.readInt(),
                    /* color    */ in.readInt());
        }

        public MensaItem[] newArray(int size) {
            return new MensaItem[size];
        }
    };

}
