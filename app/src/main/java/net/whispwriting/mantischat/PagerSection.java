package net.whispwriting.mantischat;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

class PagerSection extends FragmentPagerAdapter {

    public PagerSection (FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                RequestsFragment requestFragment = new RequestsFragment();
                return requestFragment;
            case 1:
                ChatsFragment chatFragment = new ChatsFragment();
                return chatFragment;
            case 2:
                FriendsList friendFragment = new FriendsList();
                return null;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Requests";
            case 1:
                return "Chats";
            case 2:
                return "Friends";
            default:
                return null;
        }
    }
}
