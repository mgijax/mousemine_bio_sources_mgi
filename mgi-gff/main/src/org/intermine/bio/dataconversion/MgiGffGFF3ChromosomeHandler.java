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
	// Column 1 contains values the form "chr|strain"
	// Split the value, so the chromosome primaryId is the first part and 
	// the strain is the second.
	// FIXME: need a more robust (less hacky) way to get the strain,
	// but for now this is what we have to do. 
	// (Its the only info we have access to.)
	Item seq = super.makeSequenceItem(converter, identifier);
	String [] parts = identifier.split("\\|");
	String chr  = parts[0];
	String strainName = parts[1];
	if (chr.length() == 1 && Character.isDigit(chr.charAt(0))) {
	    chr = "0" + chr;
	}    
	try {
	    seq.setAttribute("primaryIdentifier", identifier);
	    seq.setAttribute("symbol", "chr" + chr);
	    seq.setAttribute("name", "Chromosome " + chr + " (" + strainName + ")");
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
