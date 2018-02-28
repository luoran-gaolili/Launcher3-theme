package com.rgk.theme;

import java.util.ArrayList;
import java.util.List;

import com.rgk.theme.ImageAdapter.SourceUtil;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class DetailFragment extends Fragment implements OnItemSelectedListener {

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mCurrentTheme);
        Log.d(TAG, "onSaveInstanceState,and pos is:"+mCurrentTheme);
    }

    private ImageAdapter detailAdapter;
    private List<Drawable> detailSource;
    private Gallery mGallery;
    private ImageButton mOk;
    private List<ImageView> mImages;
    public static final String THEME_ACTION="com.rgk.theme.action.SWITCH_THEME";
    private int mCurrentTheme=0;

    private static final String TAG="DetailFragment";
    public static final String KEY_POSITION = "position";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG,"detaifragment,oncreateview!");
        // TODO Auto-generated method stub
        if(savedInstanceState!=null){
           int temp = savedInstanceState.getInt(KEY_POSITION, -100);
           if(temp!=-100){
               mCurrentTheme = temp;
           }
        }

        View v = inflater.inflate(R.layout.detail, container,false);
        mGallery = (Gallery)v.findViewById(R.id.gallery);
        mGallery.setAdapter(detailAdapter);
        setGallery();//add for set gallery property
        mOk = (ImageButton)v.findViewById(R.id.ok);
        mOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String name = SourceFactory.getSource().get(mCurrentTheme).getName();
                String currentName = Settings.System.getString(getActivity().getContentResolver(), "theme_switch_name");
                if(!name.equals(currentName)){
                Settings.System.putString(getActivity().getContentResolver(), "theme_switch_name", name);
                Log.d(TAG, "apply theme,and name is:"+name);
                Intent intent = new Intent(THEME_ACTION);
                getActivity().sendBroadcast(intent);
                }
                Toast.makeText(getActivity(), R.string.set_success, Toast.LENGTH_SHORT).show();
                //A:DLYQYSE-390 gu.qian@ragentek.com 20160823 start
                Intent intent_Launcher = new Intent("android.intent.action.MAIN");
                intent_Launcher.addCategory(Intent.CATEGORY_HOME);
                getActivity().startActivity(intent_Launcher);
                //A:DLYQYSE-390 gu.qian@ragentek.com 20160823 end
            }
        });
        mImages.add((ImageView)v.findViewById(R.id.pre1));
        mImages.add((ImageView)v.findViewById(R.id.pre2));
        mImages.add((ImageView)v.findViewById(R.id.pre3));
        mGallery.setOnItemSelectedListener(this);
        return v;
    }

    private void setGallery(){}

    private void setCurrentImg(int pos){
        for(int i=0;i<mImages.size();i++){
            if(i==pos)mImages.get(i).setPressed(true);
            else mImages.get(i).setPressed(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.d(TAG,"detaifragment,onCreate!");
        mCurrentTheme = getArguments().getInt(KEY_POSITION, 0);
        initDetailData();
        mImages = new ArrayList<ImageView>();
    }

    private void initDetailData(){
        detailSource = new ArrayList<Drawable>();
        getDetailSource(mCurrentTheme);
        detailAdapter = new ImageAdapter(getActivity(), detailSource);
        detailAdapter.setSourceUtil(new SourceUtil() {

            @Override
            public ImageView getView(LayoutInflater inflater, int arg0,
                    View arg1, ViewGroup arg2) {
                // TODO Auto-generated method stub
                    ImageView iv = (ImageView) inflater.inflate(R.layout.detailimage, arg2,false);
                    iv.setImageDrawable(detailSource.get(arg0));
                    return iv;
            }
        });

    }

    private void getDetailSource(int pos){
        detailSource.clear();
        detailSource.add(SourceFactory.getSource().get(pos).getKeyguard());
        detailSource.add(SourceFactory.getSource().get(pos).getPreview());
        detailSource.add(SourceFactory.getSource().get(pos).getLauncher());
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        detailAdapter.destroy();
        detailAdapter=null;
        Log.d(TAG, "detailfragment,ondestroy!");
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
		String actionBarTitle = SourceFactory.getSource().get(mCurrentTheme).getName();
		if("System".equals(actionBarTitle)){
			actionBarTitle = "Android N";
		}
        getActivity().getActionBar().setTitle(actionBarTitle);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
        // TODO Auto-generated method stub
        setCurrentImg(arg2);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

}
