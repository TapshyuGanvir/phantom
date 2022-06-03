package com.sixsimplex.treasurehunt.Phantom1.picture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sixsimplex.treasurehunt.Phantom1.picture.camera.GetAttachmentInfoInterface;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.editing.model.Attachment;

import java.io.File;
import java.util.List;

public class PictureViewAdapter extends RecyclerView.Adapter<PictureViewAdapter.ViewHolder> {

    Context context;
    List<Attachment> attachmentList;
    GetAttachmentInfoInterface getAttachmentInfoInterface;

    public PictureViewAdapter(PictureActivity pictureActivity, List<Attachment> attachmentFileList, GetAttachmentInfoInterface getAttachmentInfoInterface) {
        this.context=pictureActivity;
        this.attachmentList=attachmentFileList;
        this.getAttachmentInfoInterface=getAttachmentInfoInterface;
    }

    @NonNull
    @Override
    public PictureViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.picture_item, parent, false);
        return new PictureViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Attachment attachment = attachmentList.get(position);
        File file = attachment.getFile();

        if (file != null && file.exists()) {

            String attachmentPath = file.getAbsolutePath();
            String extension = attachmentPath.substring(attachmentPath.lastIndexOf("."));

            holder.iv_attachment_img.setTag(attachment);
            Bitmap attachmentBitmap = null;

//            if (extension.equalsIgnoreCase(AppConstants.videoExtension)) {
//                attachmentBitmap = ThumbnailUtils.createVideoThumbnail(attachmentPath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
//                videoIconView.setVisibility(View.VISIBLE);
//
//            } else if (extension.equalsIgnoreCase(AppConstants.imageExtension)) {
//
//
//                videoIconView.setVisibility(View.GONE);
//            } else if (extension.equalsIgnoreCase(AppConstants.audioExtension)) {
//
//                attachmentBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.audio_image);
//                videoIconView.setVisibility(View.VISIBLE);
//            }
            attachmentBitmap = new BitmapDrawable(context.getResources(), attachmentPath).getBitmap();

            if (attachmentBitmap != null) {
                int height = (int) (attachmentBitmap.getHeight() * (512.0 / attachmentBitmap.getWidth()));
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(attachmentBitmap, 512, height, true);
                holder.iv_attachment_img.setImageBitmap(scaledBitmap);
            }
        } else {
            holder.iv_attachment_img.setVisibility(View.GONE);
            holder.iv_delete_img.setVisibility(View.GONE);
        }

        holder.iv_delete_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAttachmentInfoInterface.deletePicture(getItemViewType(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return attachmentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_attachment_img, iv_delete_img;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_attachment_img=itemView.findViewById(R.id.iv_attachment_img);
            iv_delete_img =itemView.findViewById(R.id.iv_select_img);
        }
    }
}
