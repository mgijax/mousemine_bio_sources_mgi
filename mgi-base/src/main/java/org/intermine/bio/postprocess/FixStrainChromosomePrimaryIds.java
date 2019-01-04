package org.intermine.bio.postprocess;

import org.apache.log4j.Logger;
import org.intermine.model.bio.Chromosome;
import org.intermine.postprocess.PostProcessor;
import org.intermine.bio.util.Constants;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.SingletonResults;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.metadata.ConstraintOp;
import org.intermine.objectstore.query.ContainsConstraint;
import org.intermine.objectstore.query.QueryObjectReference;

import java.util.Iterator;

/**
 * Turns Chromosome primary identidiers like "5|A/J" into just "5".
 * During the build, strain chromosomes are given primary identifiers that include the
 * strain name, e.g., "5|A/J" is the primaryIdentifier for chromosome 5 in A/J. This
 * is done so that chromosomes from difference strains remain distinct from (ie are not 
 * merged with) each other or with the canonical chromosomes. After the mine is built,
 * but BEFORE building attribute indices (!!), this process runs to clean up the identifiers 
 * so all chromosome 5's just say "5" in their primaryIdentifier field.
 *
 * @author Joel Richardson
 */
public class FixStrainChromosomePrimaryIds extends PostProcessor
{
    private static final Logger LOG = Logger.getLogger(FixStrainChromosomePrimaryIds.class);

    public FixStrainChromosomePrimaryIds (ObjectStoreWriter osw) {
        super(osw);
    }
    public void postProcess() throws ObjectStoreException {
	LOG.info("FixStrainChromosomePrimaryIds: changing Chromosome.primaryIdentifier for strain chromosomes.");
	// new query
	ObjectStore os = osw.getObjectStore();
        Query q = new Query();
        QueryClass qcChr = new QueryClass(Chromosome.class);
	// From class Chromosome, select all chromosome instances
        q.addFrom(qcChr);
        q.addToSelect(qcChr);
	// Only want strain-chromosomes (i.e., where strain reference exists).
	QueryObjectReference strRef = new QueryObjectReference(qcChr, "strain");
        ContainsConstraint cc = new ContainsConstraint(strRef, ConstraintOp.IS_NOT_NULL);
        q.setConstraint(cc);
	// Iterate over results
	SingletonResults res = os.executeSingleton(q);
        Iterator<?> chrIter = res.iterator();
        while (chrIter.hasNext()) {
            Chromosome chr = (Chromosome) chrIter.next();
	    String pid = chr.getPrimaryIdentifier();
	    int pipx = pid.indexOf("|");
	    if (pipx != -1) {
	        String newpid = pid.substring(0,pipx);
		chr.setPrimaryIdentifier(newpid);
		LOG.info("Converted '" + pid + "' to '" + newpid + "'.");
		osw.store(chr);
	    }
        }

    }
}
