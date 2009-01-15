package org.limewire.ui.swing.library.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.limewire.core.api.FilePropertyKey;
import org.limewire.core.api.library.FileItem;
import org.limewire.core.api.library.LocalFileItem;
import org.limewire.core.api.library.LocalFileList;
import org.limewire.ui.swing.table.ColumnStateInfo;
import org.limewire.ui.swing.util.I18n;
import org.limewire.ui.swing.util.PropertyUtils;

/**
 * Table format for the Audio Table when it is in Sharing View
 */
public class SharedAudioTableFormat<T extends LocalFileItem> extends AbstractMyLibraryFormat<T> {
    static final int ACTION_INDEX = 0;
    static final int PLAY_INDEX = 1;
    static final int TITLE_INDEX = 2;
    static final int ARTIST_INDEX = 3;
    static final int ALBUM_INDEX = 4;
    static final int LENGTH_INDEX = 5;
    static final int GENRE_INDEX = 6;
    static final int BITRATE_INDEX = 7;
    static final int SIZE_INDEX = 8;
    static final int FILENAME_INDEX = 9;
    static final int TRACK_INDEX = 10;
    static final int YEAR_INDEX = 11;
    static final int QUALITY_INDEX = 12;
    static final int DESCRIPTION_INDEX = 13;
    
    private final LocalFileList localFileList;
    
    public SharedAudioTableFormat(LocalFileList localFileList) {
        super(ACTION_INDEX, new ColumnStateInfo[] {
                new ColumnStateInfo(ACTION_INDEX, "SHARE_LIBRARY_AUDIO_ACTION", I18n.tr("Sharing"), 50, true, false),
                new ColumnStateInfo(PLAY_INDEX, "SHARE_LIBRARY_AUDIO_PLAY", "", 25, false, false), 
                new ColumnStateInfo(TITLE_INDEX, "SHARE_LIBRARY_AUDIO_TITLE", I18n.tr("Name"), 260, true, true),     
                new ColumnStateInfo(ARTIST_INDEX, "SHARE_LIBRARY_AUDIO_ARTIST", I18n.tr("Artist"), 120, true, true), 
                new ColumnStateInfo(ALBUM_INDEX, "SHARE_LIBRARY_AUDIO_ALBUM", I18n.tr("Album"), 180, true, true), 
                new ColumnStateInfo(LENGTH_INDEX, "SHARE_LIBRARY_AUDIO_LENGTH", I18n.tr("Length"), 60, true, true), 
                new ColumnStateInfo(GENRE_INDEX, "SHARE_LIBRARY_AUDIO_GENRE", I18n.tr("Genre"), 60, false, true), 
                new ColumnStateInfo(BITRATE_INDEX, "SHARE_LIBRARY_AUDIO_BITRATE", I18n.tr("Bitrate"), 50, false, true), 
                new ColumnStateInfo(SIZE_INDEX, "SHARE_LIBRARY_AUDIO_SIZE", I18n.tr("Size"), 50, false, true),
                new ColumnStateInfo(FILENAME_INDEX, "SHARE_LIBRARY_AUDIO_FILENAME", I18n.tr("Filename"), 100, false, true), 
                new ColumnStateInfo(TRACK_INDEX, "SHARE_LIBRARY_AUDIO_TRACK", I18n.tr("Track"), 50, false, true), 
                new ColumnStateInfo(YEAR_INDEX, "SHARE_LIBRARY_AUDIO_YEAR", I18n.tr("Year"), 50, false, true), 
                new ColumnStateInfo(QUALITY_INDEX, "SHARE_LIBRARY_AUDIO_QUALITY", I18n.tr("Quality"), 60, false, true), 
                new ColumnStateInfo(DESCRIPTION_INDEX, "SHARE_LIBRARY_AUDIO_DESCRIPTION", I18n.tr("Description"), 100, false, true) 
        });
        this.localFileList = localFileList;
    }
    
    @Override
    public Object getColumnValue(T baseObject, int column) {
        switch(column) {
        case PLAY_INDEX: return baseObject;
        case TITLE_INDEX: return baseObject;
        case ARTIST_INDEX: return baseObject.getProperty(FilePropertyKey.AUTHOR);
        case ALBUM_INDEX: return baseObject.getProperty(FilePropertyKey.ALBUM);
        case LENGTH_INDEX: return baseObject.getProperty(FilePropertyKey.LENGTH);
        case GENRE_INDEX: return baseObject.getProperty(FilePropertyKey.GENRE);
        case BITRATE_INDEX: return baseObject.getProperty(FilePropertyKey.BITRATE);
        case FILENAME_INDEX: return baseObject.getFileName();
        case SIZE_INDEX: return baseObject.getSize();
        case TRACK_INDEX: return baseObject.getProperty(FilePropertyKey.TRACK_NUMBER);
        case YEAR_INDEX: return baseObject.getProperty(FilePropertyKey.YEAR);
        case QUALITY_INDEX: return "";
        case DESCRIPTION_INDEX: return baseObject.getProperty(FilePropertyKey.DESCRIPTION);
        case ACTION_INDEX: return baseObject;
        }
        throw new IllegalArgumentException("Unknown column:" + column);
    }

    @Override
    public Class getColumnClass(int column) {
        switch(column) {
            case ACTION_INDEX:
            case PLAY_INDEX:
                return FileItem.class;
        }
        return super.getColumnClass(column);
    }
        
    @Override
    public Comparator getColumnComparator(int column) {
        switch(column) {
            case PLAY_INDEX: return new NameComparator();
            case ACTION_INDEX: return new CheckBoxComparator(localFileList);
        }
        return super.getColumnComparator(column);
    }
    
    @Override
    public int getDefaultSortColumn() {
        return ARTIST_INDEX;
    }

    @Override
    public List<Integer> getSecondarySortColumns(int column) {
        switch (column) {
        case ARTIST_INDEX:
            return Arrays.asList(ALBUM_INDEX, TRACK_INDEX, TITLE_INDEX);
        case ALBUM_INDEX:
            return Arrays.asList(TRACK_INDEX, TITLE_INDEX);
        default:
            return Collections.emptyList();
        }
    }
    
    /**
     * Compares the title field in the NAME_COLUMN
     */
    private class NameComparator implements Comparator<LocalFileItem> {
        @Override
        public int compare(LocalFileItem o1, LocalFileItem o2) {
            String title1 = PropertyUtils.getTitle(o1);
            String title2 = PropertyUtils.getTitle(o2);
            
            return title1.toLowerCase().compareTo(title2.toLowerCase());
        }
    }
}
