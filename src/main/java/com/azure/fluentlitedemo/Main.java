package com.azure.fluentlitedemo;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.mediaservices.MediaservicesManager;
import com.azure.resourcemanager.mediaservices.models.MediaService;
import com.azure.resourcemanager.mediaservices.models.StorageAccountType;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.StorageAccount;

import java.util.Collections;

public class Main {

    private static final ClientLogger logger = new ClientLogger(Main.class);

    private static final String RG_NAME = "rg-weidxu-demo";
    private static final String SA_NAME = "sa1weidxu";
    private static final String MS_NAME = "ms1weidxu";

    public static void main(String args[]) {

        // share the same http client and credential.
        HttpClient httpClient = HttpClient.createDefault();
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // init Fluent Premium
        StorageManager storageManager = StorageManager.configure()
                .withHttpClient(httpClient)
                .authenticate(tokenCredential, new AzureProfile(AzureEnvironment.AZURE));

        // init Fluent Lite media service
        MediaservicesManager mediaservicesManager = MediaservicesManager.configure()
                .withHttpClient(httpClient)
                .authenticate(tokenCredential, new AzureProfile(AzureEnvironment.AZURE));

        logger.info("begin create storage account");

        // Fluent Premium to create resource group and storage account
        StorageAccount storageAccount = storageManager.storageAccounts().define(SA_NAME)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME)
                .create();

        logger.info("storage account created {}", storageAccount.id());

        logger.info("begin create media service");

        // Fluent Lite to create media service
        MediaService mediaService = mediaservicesManager.mediaservices().define(MS_NAME)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(RG_NAME)
                .withStorageAccounts(Collections.singletonList(new com.azure.resourcemanager.mediaservices.models.StorageAccount()
                        .withId(storageAccount.id())
                        .withType(StorageAccountType.PRIMARY)))
                .create();

        logger.info("media service created {}", mediaService.id());

        // manage resources
        mediaservicesManager.mediaservices().getById(mediaService.id());

        storageManager.storageAccounts().getById(storageAccount.id());

        // delete resources
        mediaservicesManager.mediaservices().deleteById(mediaService.id());

        storageManager.storageAccounts().deleteById(storageAccount.id());

        // clean up
        storageManager.resourceManager().resourceGroups().beginDeleteByName(RG_NAME);
    }
}
