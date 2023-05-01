package gov.epa.emissions.framework.services.persistence;

import java.io.Serializable;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.config.BeanDefinition;

@Repository
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class GenericJpaDao< T extends Serializable >
 extends AbstractJpaDao< T > implements IGenericDao< T >{
   //
}