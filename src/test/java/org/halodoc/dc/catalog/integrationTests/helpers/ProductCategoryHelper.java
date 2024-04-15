package org.halodoc.dc.catalog.integrationTests.helpers;

import com.github.tomakehurst.wiremock.client.VerificationException;
import org.testng.Assert;

import java.util.concurrent.Callable;


public class ProductCategoryHelper<T> extends BaseHelper {

    public void deleteCategoryEntityMapping(String categoryId,String productId){

    }

    public void verifyCategoryEntityMapping(String categoryId, String productId, int timeOutInSec){


        long currentTime = System.currentTimeMillis();
        boolean result = false;
        VerificationException e = new VerificationException("assert error");
        while ((System.currentTimeMillis() - currentTime) < timeOutInSec * 1000 && !result) {//dynamically wait for kafka messages to be consumed for max 10s
            try {
               //TODO: verifyDB
                result = true;
            } catch (VerificationException v) {
                e = v;
            }
        }

        Assert.assertTrue(result, e.getMessage());
    }

    public T verify( Callable<T> func) throws Exception {
        return func.call();
    }

}
