package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.SectorsDAO;

import java.util.Iterator;
import java.util.List;

public class SectorsDAOTest extends ServiceTestCase {

    private SectorsDAO dao;

    private UserDAO userDao;

    protected void doSetUp() throws Exception {
        dao = new SectorsDAO();
        userDao = new UserDAO();
    }

    protected void doTearDown() {// no op
    }

    public void testShouldGetAllSectors() {
        List sectors = dao.getAll(session);
        assertTrue(sectors.size() >= 14);
    }

    private Sector currentSector(Sector target) {
        List sectors = dao.getAll(session);
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector element = (Sector) iter.next();
            if (element.equals(target))
                return element;
        }

        return null;
    }

    public void testShouldGetSectorLock() {
        User user = userDao.get("emf", session);
        List sectors = dao.getAll(session);
        Sector sector = (Sector) sectors.get(0);

        Sector lockedSector = dao.obtainLocked(user, sector, session);
        assertEquals(lockedSector.getLockOwner(), user.getUsername());

        Sector sectorLoadedFromDb = currentSector(sector);
        assertEquals(sectorLoadedFromDb.getLockOwner(), user.getUsername());
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        User owner = userDao.get("emf", session);
        List sectors = dao.getAll(session);
        Sector sector = (Sector) sectors.get(0);

        dao.obtainLocked(owner, sector, session);

        User user = userDao.get("admin", session);
        Sector result = dao.obtainLocked(user, sector, session);

        assertTrue(result.isLocked(owner));// locked by owner
        assertFalse(result.isLocked(user));// failed to obtain lock for another user
    }

    public void testShouldReleaseSectorLock() {
        User owner = userDao.get("emf", session);
        List sectors = dao.getAll(session);
        Sector sector = (Sector) sectors.get(0);

        Sector lockedSector = dao.obtainLocked(owner, sector, session);
        Sector releasedSector = dao.releaseLocked(owner, lockedSector, session);
        assertFalse("Should have released lock", releasedSector.isLocked());

        Sector sectorLoadedFromDb = currentSector(sector);
        assertFalse("Should have released lock", sectorLoadedFromDb.isLocked());
    }

    public void testShouldUpdateSector() throws EmfException {
        List<Sector> sectors = dao.getAll(session);
        Sector sector = sectors.get(0);
        String name = sector.getName();

        User owner = userDao.get("emf", session);

        Sector modifiedSector1 = dao.obtainLocked(owner, sector, session);
        assertEquals(modifiedSector1.getLockOwner(), owner.getUsername());
        modifiedSector1.setName("TEST");

        Sector modifiedSector2 = dao.update(modifiedSector1, session);
        assertEquals("TEST", modifiedSector1.getName());
        assertEquals(modifiedSector2.getLockOwner(), null);

        // restore
        Sector modifiedSector = dao.obtainLocked(owner, sector, session);
        modifiedSector.setName(name);

        Sector modifiedSector3 = dao.update(modifiedSector, session);
        assertEquals(sector.getName(), modifiedSector3.getName());
    }

}
