package org.intermine.bio.dataconversion;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.intermine.bio.io.gff3.GFF3Record;
import org.intermine.metadata.Model;
import org.intermine.xml.full.Item;
import java.lang.RuntimeException;
import org.intermine.objectstore.ObjectStoreException;

public class MgiGffGFF3ChromosomeHandler extends GFF3SeqHandler
{
    private Map<String,String> strains = new HashMap<String,String>();

    public Item makeSequenceItem(GFF3Converter converter, String identifier) {
	Item seq = super.makeSequenceItem(converter, identifier);
	String strainName = identifier.split("\\|")[1];
	try {
	    String sref = getStrainRef(strainName,converter);
	    seq.setReference("strain", sref);
	} catch (ObjectStoreException e) {
	    throw new RuntimeException("Got objectStore exception.", e);
	}
	return seq;
    }
    public String getStrainRef(String n, GFF3Converter converter) throws ObjectStoreException {
	String strainRef = strains.get(n);
	if (strainRef == null) {
	    Item strain = converter.createItem("Strain");
	    strain.setAttribute("name", n);
	    strainRef = strain.getIdentifier();
	    strains.put(n, strainRef);
	    converter.store(strain);
	}
	return strainRef;
    }
}
