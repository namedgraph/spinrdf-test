
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ResourceFactory;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spinrdf.arq.ARQFactory;
import org.spinrdf.constraints.ConstraintViolation;
import org.spinrdf.constraints.SPINConstraints;
import org.spinrdf.system.SPINModuleRegistry;
import org.spinrdf.vocabulary.SPIN;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ConstraintTest {
    
    private static OntModel ontModel;
    
    @BeforeClass
    public static void init()
    {
        SPINModuleRegistry.get().init();
        ARQFactory.get().setUseCaches(false);

        // the ontology URI is mapped in location-mapping.n3 to src/main/com/atomgraph/processor/http-tests/custom/ontology.ttl
        ontModel = OntDocumentManager.getInstance().getOntology("https://github.com/AtomGraph/Processor/blob/develop/http-tests/custom#", OntModelSpec.OWL_MEM);
        //ontModel.writeAll(System.out, "TURTLE");
        
        SPINModuleRegistry.get().registerAll(ontModel, null);
    }
    
    @Test
    public void testConstraints() {
        List<ConstraintViolation> cvs = SPINConstraints.check(ontModel, null);
        assertEquals(0, cvs.size());
    }
    
    @Test
    public void testQueryExec() {
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add("TYPE_CLASS", SPIN.Template);
        qsm.add("predicate", SPIN.body);
        qsm.add("comment", ResourceFactory.createPlainLiteral("the body of the Template"));
        qsm.add("minCount", ResourceFactory.createTypedLiteral("0", XSDDatatype.XSDinteger));
        qsm.add("maxCount", ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        
        try
        {
            String queryString = IOUtils.toString(getClass().getResource("test.rq"), StandardCharsets.UTF_8);
            Query query = QueryFactory.create(queryString);
            QueryExecution qex = QueryExecutionFactory.create(query, ontModel, qsm);
            ResultSet results = qex.execSelect();
            
            assertEquals(0, ResultSetFormatter.consume(results));
        }
        catch (IOException ex)
        {
            ex.printStackTrace(System.out);
        }
    }
}
