package com.sixsimplex.treasurehunt.Phantom1.appfragment.profile;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sixsimplex.treasurehunt.Phantom1.deliveryservice.DeliveryService;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.Phantom1.app.view.DeliveryMainActivity;
import com.sixsimplex.treasurehunt.Phantom1.utils.IrefreshResponce;
import com.sixsimplex.treasurehunt.revelocore.editprofile.GetProfilePicAsyncTask;
import com.sixsimplex.treasurehunt.revelocore.login.view.LoginActivity;
import com.sixsimplex.treasurehunt.revelocore.principalEndpoint.view.IPrincipleEndpointView;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.view.ISurveyDetails;
import com.sixsimplex.treasurehunt.revelocore.userProfile.FetchUserProfileListener;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.ToastUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    Context context;
    TextView username_tv, name, mobile_tv;
    CircleImageView profileImage;
    Button signOut,refreshAssignment;
    ProfilePresenter profilePresenter;

    public ProfileFragment(DeliveryMainActivity context, ProfilePresenter profileFragment) {
        this.context=context;
        this.profilePresenter=profileFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button refreshdata = (Button) view.findViewById(R.id.refreshdata);
        username_tv = view.findViewById(R.id.username_tv);
        name = view.findViewById(R.id.name);
        mobile_tv = view.findViewById(R.id.mobile_tv);
        profileImage = view.findViewById(R.id.profileImage);
        signOut = view.findViewById(R.id.signOut);
        refreshAssignment=view.findViewById(R.id.refreshAssignment);
        setValues();
        refreshAssignment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(!DeliveryService.isDeliveryServiceRunning((ActivityManager) requireActivity().getSystemService(ACTIVITY_SERVICE))){
                        ((DeliveryMainActivity) requireActivity()).showProgressDialog("Refreshing Assignment");
                       profilePresenter.refreshAssignment(requireActivity(), new IrefreshResponce() {
                           @Override
                           public void onSuccessResponse() {
                               DeliveryDataModel.getInstance().clearAll();
                               ((DeliveryMainActivity) requireActivity()).hideProgressDialog();
                               requireActivity().finish();
                               requireActivity().startActivity(requireActivity().getIntent());
                           }

                           @Override
                           public void onFailedResponse(String message) {
                               ((DeliveryMainActivity) requireActivity()).hideProgressDialog();
                           }
                       }, true, true, new IPrincipleEndpointView() {
                           @Override
                           public void onPrincipalEndPointSuccess(int request, boolean downloadRedb) {

                           }

                           @Override
                           public void onPrincipalEndPointError(int request, String errorMessage) {

                           }
                       }, new ISurveyDetails() {
                           @Override
                           public void onError(String message) {

                           }

                           @Override
                           public void onSuccess() {

                           }
                       });

                    }else{
                        ToastUtility.toast("Please stop Your Delivery before Refresh Assignment",requireActivity(),true);
                    }

                }catch (Exception e){
                    ((DeliveryMainActivity) requireActivity()).hideProgressDialog();
                    e.printStackTrace();
                }
            }
        });
        return view;
    }


    private void setValues() {
        try {
            username_tv.setText(UserInfoPreferenceUtility.getUserName());
            name.setText(UserInfoPreferenceUtility.getFirstName() + " " + UserInfoPreferenceUtility.getLastName());
            mobile_tv.setText(UserInfoPreferenceUtility.getPhoneNumber());

            String mmFileName = UserInfoPreferenceUtility.getUserName() + "ProfilePic.png";
            File mmProfilePic = new File(AppFolderStructure.userProfilePictureFolderPath(context) + File.separator + mmFileName);
            if (mmProfilePic.exists()) {
                profilePresenter.getFirstTimeOpenProfile();
                if (profilePresenter.getFirstTimeOpenProfile()) {
                    new GetProfilePicAsyncTask(getActivity(), UserInfoPreferenceUtility.getUserName(), profileImage).execute();
                    profilePresenter.setFirstTimeOpenProfile(false);
                } else {
                    Bitmap myBitmap = BitmapFactory.decodeFile(mmProfilePic.getAbsolutePath());
                    if (myBitmap != null) {
                        profileImage.setImageBitmap(myBitmap);
                    } else {
                        Bitmap UserIconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_icon);
                        profileImage.setImageBitmap(UserIconBitmap);
                    }
                }
            } else {
                if (NetworkUtility.checkNetworkConnectivity(context)) {
                    new GetProfilePicAsyncTask(getActivity(), UserInfoPreferenceUtility.getUserName(), profileImage).execute();
                }
            }

            signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!DeliveryService.isDeliveryServiceRunning((ActivityManager) requireActivity().getSystemService(ACTIVITY_SERVICE))){
                        logoutRequest();
                    }else{
                        ToastUtility.toast("Please stop Your Delivery before Logout",requireActivity(),true);
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void logoutRequest() {
        AppMethods.clearAllStaticData();
        Intent HomeEntityGridActivityIntent = new Intent(context, LoginActivity.class);
        HomeEntityGridActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(HomeEntityGridActivityIntent);
        requireActivity().finish();

    }

    @Override
    public void onStart() {
        super.onStart();
//        ((DeliveryMainActivity) requireActivity()).showHideButtonTabLayout(false);
    }

    @Override
    public void onStop() {
        super.onStop();
//        ((DeliveryMainActivity) requireActivity()).showHideButtonTabLayout(true);
    }
}