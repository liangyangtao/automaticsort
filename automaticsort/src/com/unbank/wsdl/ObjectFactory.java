
package com.unbank.wsdl;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.unbank.wsdl package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Comparesimilarity_QNAME = new QName("http://cxfinterface.unbank.com/", "comparesimilarity");
    private final static QName _ComparesimilarityResponse_QNAME = new QName("http://cxfinterface.unbank.com/", "comparesimilarityResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.unbank.wsdl
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ComparesimilarityResponse }
     * 
     */
    public ComparesimilarityResponse createComparesimilarityResponse() {
        return new ComparesimilarityResponse();
    }

    /**
     * Create an instance of {@link Comparesimilarity }
     * 
     */
    public Comparesimilarity createComparesimilarity() {
        return new Comparesimilarity();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Comparesimilarity }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://cxfinterface.unbank.com/", name = "comparesimilarity")
    public JAXBElement<Comparesimilarity> createComparesimilarity(Comparesimilarity value) {
        return new JAXBElement<Comparesimilarity>(_Comparesimilarity_QNAME, Comparesimilarity.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ComparesimilarityResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://cxfinterface.unbank.com/", name = "comparesimilarityResponse")
    public JAXBElement<ComparesimilarityResponse> createComparesimilarityResponse(ComparesimilarityResponse value) {
        return new JAXBElement<ComparesimilarityResponse>(_ComparesimilarityResponse_QNAME, ComparesimilarityResponse.class, null, value);
    }

}
