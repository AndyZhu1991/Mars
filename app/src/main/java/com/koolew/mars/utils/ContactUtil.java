package com.koolew.mars.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jinchangzhu on 6/29/15.
 */
public class ContactUtil {

    public static List<SimpleContactInfo> getPhoneContacts(Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactsCur = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (contactsCur.getCount() == 0) {
            return null;
        }

        List<SimpleContactInfo> contacts = new LinkedList<SimpleContactInfo>();

        while (contactsCur.moveToNext()) {
            String id = contactsCur.getString(
                    contactsCur.getColumnIndex(ContactsContract.Contacts._ID));
            String name = contactsCur.getString(
                    contactsCur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if (Integer.parseInt(contactsCur.getString(contactsCur.getColumnIndex(
                    ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor cursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[] { id },
                        null);
                while (cursor.moveToNext()) {
                    String phoneNo = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contacts.add(new SimpleContactInfo(name, phoneNo));
                }
                cursor.close();
            }
        }

        return contacts;
    }

    public static class SimpleContactInfo {
        private String name;
        private String number;

        SimpleContactInfo(String name, String number) {
            this.name = name;
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }
    }
}