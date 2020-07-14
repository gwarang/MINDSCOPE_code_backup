package kr.ac.inha.mindscope;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import kr.ac.inha.mindscope.fragment.CareChildFragment1;
import kr.ac.inha.mindscope.fragment.CareChildFragment2;
import kr.ac.inha.mindscope.fragment.CareChildFragment3;

public class CareViewPagerAdapter extends FragmentStateAdapter {

    private static final int CHILD1 = 0;
    private static final int CHILD2 = 1;
    private static final int CHILD3 = 2;

    public CareViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case CHILD1:
                return new CareChildFragment1();
            case CHILD2:
                return new CareChildFragment2();
            case CHILD3:
                return new CareChildFragment3();
            default:
                return new CareChildFragment1();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
