package hadyelmahrangy.com.photoapp.imageEditor.adapters.filters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import hadyelmahrangy.com.photoapp.R;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter;

public class ImageFiltersAdapter extends RecyclerView.Adapter {

    private GPUImageFilter[] filterItems = new GPUImageFilter[12];
    private String[] filterNames;
    private int[] filterImages = {
            R.drawable.filter_normal,
            R.drawable.filter_bw,
            R.drawable.filter_gotham,
            R.drawable.filter_lkelvin,
            R.drawable.filter_1977,
            R.drawable.filter_brannan,
            R.drawable.filter_hefe,
            R.drawable.filter_nashville,
            R.drawable.filter_xpro2,
            R.drawable.filter_amatorka,
            R.drawable.filter_missetikate,
            R.drawable.filter_softelegance};

    private ImageFilterClickListener mListener;

    public ImageFiltersAdapter(Context context, ImageFilterClickListener l) {
        mListener = l;

        filterItems[0] = null;
        filterItems[1] = new GPUImageGrayscaleFilter();

        GPUImageToneCurveFilter gpuImageToneCurveFilter = new GPUImageToneCurveFilter();
        gpuImageToneCurveFilter.setFromCurveFileInputStream(context.getResources().openRawResource(R.raw.gotham));
        filterItems[2] = gpuImageToneCurveFilter;

        filterItems[3] = new IFLordKelvinFilter(context);
        filterItems[4] = new IF1977Filter(context);
        filterItems[5] = new IFBrannanFilter(context);
        filterItems[6] = new IFHefeFilter(context);
        filterItems[7] = new IFNashvilleFilter(context);
        filterItems[8] = new IFXprollFilter(context);

        GPUImageLookupFilter amatorka = new GPUImageLookupFilter();
        amatorka.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.lookup_amatorka));
        filterItems[9] = amatorka;

        GPUImageLookupFilter missEtikate = new GPUImageLookupFilter();
        missEtikate.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.lookup_miss_etikate));
        filterItems[10] = missEtikate;

        GPUImageLookupFilter softElegance = new GPUImageLookupFilter();
        softElegance.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.lookup_soft_elegance_1));
        filterItems[11] = softElegance;

        filterNames = context.getResources().getStringArray(R.array.filters_names);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false);
        ImageFiltersAdapter.ViewHolder holder = new ImageFiltersAdapter.ViewHolder(convertView);
        holder.root = convertView.findViewById(R.id.holder_root);
        holder.imageView = convertView.findViewById(R.id.img_id);
        holder.name = convertView.findViewById(R.id.filter_name);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final GPUImageFilter filter = filterItems[position];
        ViewHolder viewHolder = (ViewHolder) holder;

        if (position >= 0 && filterNames.length > position) {
            viewHolder.name.setText(filterNames[position]);
        }

        if (position >= 0 && filterImages.length > position) {
            int filterImage = filterImages[position];
            if (filterImage > 0) {
                viewHolder.imageView.setImageResource(filterImage);
            }
        }

        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFilterClick(filter);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return filterItems.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View root;
        ImageView imageView;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
