package de.unisaarland.UniApp.utils.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.TextView;

public abstract class RemoteOrLocalViewAdapter implements Parcelable {

    protected interface RemoteOrLocalViewBuilder {
        Context getContext();

        void setLayout(int layoutId);

        void setTextViewText(int viewId, CharSequence text);
        void setViewVisibility(int viewId, int visibility);
        void setBackgroundColor(int viewId, int color);
        void setOnClickIntent(int viewId, Intent intent);
        void setOnItemClickIntent(int viewId, PendingIntent intent);
        void setItemFillInIntent(int viewId, Intent intent);
        void setAdapterOrUpdate(int viewId, RemoteOrLocalViewAdapter newAdapter);
    }

    private class RemoteViewBuilder implements RemoteOrLocalViewBuilder {
        private final Context context;
        private final boolean expandAdapters;
        protected RemoteViews view;

        private RemoteViewBuilder(Context context, boolean expandAdapters) {
            this.context = context;
            this.expandAdapters = expandAdapters;
        }

        public RemoteViews getView() {
            return view;
        }

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public void setLayout(int layoutId) {
            if (view != null)
                throw new AssertionError("Only call setLayout once");
            view = new RemoteViews(context.getPackageName(), layoutId);
        }

        @Override
        public void setTextViewText(int viewId, CharSequence text) {
            view.setTextViewText(viewId, text);
        }

        @Override
        public void setViewVisibility(int viewId, int visibility) {
            view.setViewVisibility(viewId, visibility);
        }

        @Override
        public void setBackgroundColor(int viewId, int color) {
            view.setInt(viewId, "setBackgroundColor", color);
        }

        @Override
        public void setOnClickIntent(int viewId, Intent intent) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(viewId, pendingIntent);
        }

        @Override
        public void setOnItemClickIntent(int viewId, PendingIntent intent) {
            view.setPendingIntentTemplate(viewId, intent);
        }

        @Override
        public void setItemFillInIntent(int viewId, Intent intent) {
            view.setOnClickFillInIntent(viewId, intent);
        }

        @Override
        public void setAdapterOrUpdate(int viewId, RemoteOrLocalViewAdapter newAdapter) {
            if (expandAdapters) {
                RemoteViewsService.RemoteViewsFactory fac = newAdapter.asRemoteViewsFactory(
                        getContext(), true);
                for (int i = 0, e = fac.getCount(); i != e; ++i) {
                    view.addView(viewId, fac.getViewAt(i));
                }
                return;
            }

            Intent intent = new Intent(context, RemoteService.class);
            Parcel parcel = Parcel.obtain();
            parcel.writeParcelable(newAdapter, 0);
            byte[] adapterBytes = parcel.marshall();
            parcel.recycle();
            intent.putExtra("adapter", adapterBytes);
            view.setRemoteAdapter(viewId, intent);
        }
    }

    private final class LocalViewBuilder implements RemoteOrLocalViewBuilder {
        private final Context context;
        private View view;

        public LocalViewBuilder(Context context, View convertView) {
            this.context = context;
            this.view = convertView;
        }

        public View getView() {
            if (view == null)
                throw new AssertionError("Call setLayout before");
            return view;
        }

        public View findViewById(int viewId) {
            return getView().findViewById(viewId);
        }

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public void setLayout(int layoutId) {
            if (view == null)
                view = View.inflate(context, layoutId, null);
        }

        @Override
        public void setTextViewText(int viewId, CharSequence text) {
            TextView textView = (TextView) view.findViewById(viewId);
            textView.setText(text);
        }

        @Override
        public void setViewVisibility(int viewId, int visibility) {
            findViewById(viewId).setVisibility(visibility);
        }

        @Override
        public void setBackgroundColor(int viewId, int color) {
            findViewById(viewId).setBackgroundColor(color);
        }

        @Override
        public void setOnClickIntent(int viewId, final Intent intent) {
            findViewById(viewId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public void setOnItemClickIntent(int viewId, final PendingIntent intent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setItemFillInIntent(int viewId, Intent intent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAdapterOrUpdate(int viewId, RemoteOrLocalViewAdapter newAdapter) {
            ListView list = (ListView) findViewById(viewId);
            LocalAdapter adap = (LocalAdapter) list.getAdapter();
            if (adap == null)
                list.setAdapter(newAdapter.asLocalAdapter(context));
            else
                adap.update(newAdapter);
        }
    }

    public final class LocalAdapter extends BaseAdapter {
        private final Context context;

        private LocalAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return RemoteOrLocalViewAdapter.this.getCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LocalViewBuilder builder = new LocalViewBuilder(context, convertView);
            buildView(position, builder);
            return builder.getView();
        }

        public void update(RemoteOrLocalViewAdapter newAdapter) {
            RemoteOrLocalViewAdapter.this.update(newAdapter);
        }
    }

    private final class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private final Context context;
        private final boolean expandAdapters;

        private RemoteViewsFactory(Context context, boolean expandAdapters) {
            this.context = context;
            this.expandAdapters = expandAdapters;
        }

        @Override
        public void onCreate() { /* nop */ }

        @Override
        public void onDataSetChanged() { /* nop */ }

        @Override
        public void onDestroy() { /* nop */ }

        @Override
        public int getCount() {
            return RemoteOrLocalViewAdapter.this.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViewBuilder builder = new RemoteViewBuilder(context, expandAdapters);
            buildView(position, builder);
            return builder.getView();
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }

    public static class RemoteService extends RemoteViewsService {
        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
            byte[] adapterBytes = intent.getByteArrayExtra("adapter");
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(adapterBytes, 0, adapterBytes.length);
            parcel.setDataPosition(0);
            RemoteOrLocalViewAdapter adapter = parcel.readParcelable(this.getClassLoader());
            parcel.recycle();
            return adapter.asRemoteViewsFactory(this, false);
        }
    }

    private LocalAdapter localAdapter;

    protected abstract int getCount();

    protected abstract void buildView(int position, RemoteOrLocalViewBuilder builder);

    protected abstract void update(RemoteOrLocalViewAdapter newAdapter);

    public BaseAdapter asLocalAdapter(Context context) {
        if (localAdapter != null)
            // if necessary, add support for more than one
            throw new AssertionError("Only request one local adapter per instance");
        return localAdapter = new LocalAdapter(context);
    }

    public RemoteViewsService.RemoteViewsFactory asRemoteViewsFactory(Context context,
                                                                      boolean expandAdapters) {
        return new RemoteViewsFactory(context, expandAdapters);
    }

    protected void notifyDataSetChanged() {
        if (localAdapter != null)
            localAdapter.notifyDataSetChanged();
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
