package secretbox.alisha.joshua.secretbox;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class tabsPagerAdapter extends FragmentPagerAdapter {


    public tabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 3;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "REQUESTS";

            case 1:
                return "CHATS ☰";

            case 2:
                return "FRIENDS";

            default:
                return null;
        }
    }
}
