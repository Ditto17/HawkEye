package com.example.ditto.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionPagerAdapter extends FragmentPagerAdapter{

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        //setting positions of tabs
        switch (position){

            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            case 1:
                ChatsFragments chatsFragments = new ChatsFragments();
                return chatsFragments;

            case 2:
                FriendsFragments friendsFragments = new FriendsFragments();
                return friendsFragments;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }
        //defining names on tabs
    public  CharSequence getPageTitle(int position){

        switch (position){

            case 0:
                return "REQUESTS";

            case 1:
                return "CHATS";

            case 2:
                return "FRIENDS";

            default:
                return null;
        }

    }
}
