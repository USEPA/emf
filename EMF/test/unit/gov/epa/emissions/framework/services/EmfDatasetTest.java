package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.Date;

import junit.framework.TestCase;

public class EmfDatasetTest extends TestCase {
    public void testShouldBeLockedOnlyIfUsernameAndDateIsSet() {
        Lockable locked = new EmfDataset();
        locked.setLockOwner("user");
        locked.setLockDate(new Date());
        assertTrue("Should be locked", locked.isLocked());

        Lockable unlockedAsOnlyUsernameIsSet = new Sector();
        unlockedAsOnlyUsernameIsSet.setLockOwner("user");
        assertFalse("Should be unlocked", unlockedAsOnlyUsernameIsSet.isLocked());

        Lockable unlockedAsOnlyLockedDateIsSet = new Sector();
        unlockedAsOnlyLockedDateIsSet.setLockDate(new Date());
        assertFalse("Should be unlocked", unlockedAsOnlyLockedDateIsSet.isLocked());
    }

    public void testShouldBeLockedIfUsernameMatches() throws Exception {
        Lockable locked = new EmfDataset();
        locked.setLockOwner("user");
        locked.setLockDate(new Date());

        User lockedByUser = new User();
        lockedByUser.setUsername("user");
        assertTrue("Should be locked", locked.isLocked(lockedByUser));

        User notLockedByUser = new User();
        notLockedByUser.setUsername("user2");
        assertFalse("Should not be locked", locked.isLocked(notLockedByUser));
    }
    
    public void testShouldGiveCorrectCommentSettings() {
        DatasetType type = new DatasetType();
        Dataset dataset = new EmfDataset();
        KeyVal keyval1 = new KeyVal();
        KeyVal keyval2 = new KeyVal();
        KeyVal keyval3 = new KeyVal();
        KeyVal keyval4 = new KeyVal();
        
        keyval1.setKeyword(new Keyword(Dataset.header_comment_key));
        keyval1.setValue("false");
        keyval1.setValue("YES");
        keyval2.setKeyword(new Keyword(Dataset.header_comment_char));
        keyval2.setValue("%");
        
        keyval3.setKeyword(new Keyword(Dataset.inline_comment_key));
        keyval3.setValue("false");
        keyval3.setValue("TRuE");
        keyval4.setKeyword(new Keyword(Dataset.inline_comment_char));
        keyval4.setValue("$");
        
        ((EmfDataset)dataset).setDatasetType(type);
        ((EmfDataset)dataset).addKeyVal(keyval1);
        ((EmfDataset)dataset).addKeyVal(keyval2);
        ((EmfDataset)dataset).addKeyVal(keyval3);
        ((EmfDataset)dataset).addKeyVal(keyval4);
        
        assertTrue(dataset.getHeaderCommentsSetting());
        assertTrue(dataset.getInlineCommentSetting());
        assertTrue(dataset.getHeaderCommentChar().equals("%"));
        assertTrue(dataset.getInlineCommentChar().equals("$"));
    }
    
    public void testShouldGiveCorrectCommentSettingsWithBlankKeyValues() {
        Dataset dataset = new EmfDataset();
        DatasetType type = new DatasetType();
        KeyVal keyval1 = new KeyVal();
        KeyVal keyval2 = new KeyVal();
        KeyVal keyval3 = new KeyVal();
        KeyVal keyval4 = new KeyVal();
        
        keyval1.setKeyword(new Keyword(Dataset.header_comment_key));
        keyval1.setValue("false");
        keyval2.setKeyword(new Keyword(Dataset.header_comment_char));
        keyval2.setValue("");
        
        keyval3.setKeyword(new Keyword(Dataset.inline_comment_key));
        keyval3.setValue("false");
        keyval3.setValue("yes");
        keyval4.setKeyword(new Keyword(Dataset.inline_comment_char));
        keyval4.setValue("");
        
        ((EmfDataset)dataset).setDatasetType(type);
        ((EmfDataset)dataset).addKeyVal(keyval1);
        ((EmfDataset)dataset).addKeyVal(keyval2);
        ((EmfDataset)dataset).addKeyVal(keyval3);
        ((EmfDataset)dataset).addKeyVal(keyval4);
        
        assertFalse(dataset.getHeaderCommentsSetting());
        assertTrue(dataset.getInlineCommentSetting());
        assertTrue(dataset.getHeaderCommentChar().equals("#"));
        assertTrue(dataset.getInlineCommentChar().equals("!"));
    }
}
