package org.limewire.ui.swing.library.table;

import java.awt.Component;
import java.awt.Font;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Resource;
import org.limewire.core.api.Category;
import org.limewire.core.api.library.LocalFileItem;
import org.limewire.core.settings.LibrarySettings;
import org.limewire.ui.swing.components.HyperlinkButton;
import org.limewire.ui.swing.table.TableRendererEditor;
import org.limewire.ui.swing.util.GuiUtils;
import org.limewire.ui.swing.util.I18n;
import org.limewire.xmpp.api.client.XMPPService;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class ShareTableRendererEditor extends TableRendererEditor implements Configurable{
    @Resource
    private Font shareButtonFont;

    private HyperlinkButton shareButton;
    private LocalFileItem fileItem;
    
    private final XMPPService xmppService;
    
    @AssistedInject
    public ShareTableRendererEditor(@Assisted Action shareAction, XMPPService xmppService){
        GuiUtils.assignResources(this);
        this.xmppService = xmppService;
        
        shareButton = new HyperlinkButton(I18n.tr("share"));
        shareButton.setFont(shareButtonFont);
        shareButton.setHorizontalTextPosition(SwingConstants.LEFT);       
        shareButton.addActionListener(shareAction);
        
        setLayout(new MigLayout("insets 2 5 2 5, hidemode 0, aligny 50%"));
        add(shareButton);
    }

    @Override
    public Component doTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        configure((LocalFileItem)value, isSelected);
        return this;
    }

    @Override
    public Component doTableCellEditorComponent(JTable table, Object value, boolean isSelected,
            int row, int column) {
        configure((LocalFileItem)value, true);
        return this;
    }
    
    @Override
    public void configure(LocalFileItem item, boolean isRowSelected) {
        fileItem = item;

        int friendCount = item.getFriendShareCount();

        if(!item.isShareable() || item.isIncomplete()) {
            shareButton.setEnabled(item.isShareable() && !item.isIncomplete());
            shareButton.setToolTipText(I18n.tr("This file cannot be shared."));
        } else if(item.getCategory() == Category.DOCUMENT && (!LibrarySettings.ALLOW_DOCUMENT_GNUTELLA_SHARING.getValue() && !xmppService.isLoggedIn())) {
            //if the share documents with gnutella option is unchecked, the user must be logged in for the share button to be enabled.
            shareButton.setEnabled(false);
            shareButton.setToolTipText(I18n.tr("Sign in to share Documents with your friends"));
        } else if(item.getCategory() == Category.PROGRAM && !LibrarySettings.ALLOW_PROGRAMS.getValue()) {
            shareButton.setEnabled(false);
        } else {
            shareButton.setEnabled(true);
            shareButton.setToolTipText(I18n.tr("Share this file with a friend"));
        }
        
        if(friendCount > 0) {   
            shareButton.setText(I18n.tr("share ({0})",GuiUtils.toLocalizedInteger(item.getFriendShareCount())));
        } else {
            shareButton.setText(I18n.tr("share"));
            shareButton.setIcon(null);
            shareButton.setFont(shareButtonFont);
        }
    }

    public LocalFileItem getLocalFileItem() {
        return fileItem;
    }
    
    public JButton getShareButton(){
        return shareButton;
    }
}
