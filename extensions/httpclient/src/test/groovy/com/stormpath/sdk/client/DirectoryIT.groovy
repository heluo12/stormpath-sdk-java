/*
 * Copyright 2014 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.client

import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import com.stormpath.sdk.account.Account
import com.stormpath.sdk.account.Accounts
import com.stormpath.sdk.directory.Directories
import com.stormpath.sdk.directory.Directory
import com.stormpath.sdk.directory.PasswordPolicy
import com.stormpath.sdk.lang.Duration
import com.stormpath.sdk.mail.EmailStatus
import com.stormpath.sdk.provider.GoogleProvider
import com.stormpath.sdk.provider.Providers
import org.testng.annotations.Test
import java.text.DateFormat
import java.util.concurrent.TimeUnit

import static org.testng.Assert.*

/**
 *
 * @since 0.8.1
 */
class DirectoryIT extends ClientIT {

    /**
     * Asserts fix for <a href="https://github.com/stormpath/stormpath-sdk-java/pull/22">Pull Request 22</a>.
     */
    @Test
    void testCreateAndDeleteDirectory() {

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateAndDeleteDirectory")
        dir = client.currentTenant.createDirectory(dir);
        deleteOnTeardown(dir)

        assertNotNull dir.href
    }


    /**
     * Asserts fix for <a href="https://github.com/stormpath/stormpath-sdk-java/issues/12">Issue #12</a>
     */
    @Test
    void testDeleteAccount() {

        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testDeleteAccount")
        dir = client.currentTenant.createDirectory(dir)
        deleteOnTeardown(dir)

        def email = 'johndeleteme@nowhere.com'

        Account account = client.instantiate(Account)
        account = account.setGivenName('John')
            .setSurname('DELETEME')
            .setEmail(email)
            .setPassword('Changeme1!')

        dir.createAccount(account)

        String href = account.href

        //verify it was created:
        Account retrieved = dir.getAccounts(Accounts.where(Accounts.email().eqIgnoreCase(email))).iterator().next()
        assertEquals(href, retrieved.href)

        //test delete:
        retrieved.delete()

        def list = dir.getAccounts(Accounts.where(Accounts.email().eqIgnoreCase(email)))
        assertFalse list.iterator().hasNext() //no results
    }


    /**
     * Asserts <a href="https://github.com/stormpath/stormpath-sdk-java/issues/58">Issue 58</a>.
     * @since 1.0.RC
     */
    @Test
    void testCreateDirectoryViaTenantActions() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateDirectoryViaTenantActions")
        dir = client.createDirectory(dir);
        deleteOnTeardown(dir)
        assertNotNull dir.href
    }

    /**
     * @since 1.0.RC
     */
    @Test
    void testCreateDirectoryRequestViaTenantActions() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateDirectoryRequestViaTenantActions")
        GoogleProvider provider = client.instantiate(GoogleProvider.class)
        provider.setClientId("616598318417021").setClientSecret("c0ad961d45fdc0310c1c7d67c8f1d800")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.GOOGLE.builder()
                    .setClientId("616598318417021")
                    .setClientSecret("c0ad961d45fdc0310c1c7d67c8f1d800")
                    .setRedirectUri("http://localhost")
                    .build()
                ).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
    }

    /**
     * @since 1.0.0
     */
    @Test
    void testCreateLinkedInDirectoryRequestViaTenantActions() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testCreateLinkedInDirectoryRequest")

        def request = Directories.newCreateRequestFor(dir)
                .forProvider(Providers.LINKEDIN.builder()
                .setClientId("73i1dq2fko01s2")
                .setClientSecret("wJhXc81l63qEOc43")
                .build()
        ).build()
        dir = client.createDirectory(request);
        deleteOnTeardown(dir)
        assertNotNull dir.href
    }

    /**
     * @since 1.0.RC
     */
    @Test
    void testGetDirectoriesViaTenantActions() {
        def dirList = client.getDirectories()
        assertNotNull dirList.href
    }

    /**
     * @since 1.0.RC
     */
    @Test
    void testGetDirectoriesWithMapViaTenantActions() {
        def map = new HashMap<String, Object>()
        def dirList = client.getDirectories(map)
        assertNotNull dirList.href
    }

    /**
     * @since 1.0.RC
     */
    @Test
    void testGetDirectoriesWithDirCriteriaViaTenantActions() {
        def dirCriteria = Directories.criteria()
        def dirList = client.getDirectories(dirCriteria)
        assertNotNull dirList.href
    }

    /**
     * @since 1.0.0
     */
    @Test
    void testGetDirectoriesWithCustomData() {
        Directory directory = client.instantiate(Directory)
        directory.name = uniquify("Java SDK: DirectoryIT.testGetDirectoriesWithCustomData")
        directory.customData.put("someKey", "someValue")
        directory = client.createDirectory(directory);
        deleteOnTeardown(directory)
        assertNotNull directory.href

        def dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.getName())).withCustomData())

        def count = 0
        for (Directory dir : dirList) {
            count++
            assertNotNull(dir.getHref())
            assertEquals(dir.getCustomData().size(), 4)
        }
        assertEquals(count, 1)
    }

    /**
     * @since 1.0.RC4
     */
    @Test
    void testPasswordPolicy() {
        Directory dir = client.instantiate(Directory)
        dir.name = uniquify("Java SDK: DirectoryIT.testPasswordPolicy")
        dir = client.createDirectory(dir);
        deleteOnTeardown(dir)
        def passwordPolicy = dir.getPasswordPolicy()
        assertNotNull passwordPolicy.href
        assertEquals passwordPolicy.getResetTokenTtlHours(), 24
        assertEquals passwordPolicy.getResetEmailStatus(), EmailStatus.ENABLED
        assertEquals passwordPolicy.getResetSuccessEmailStatus(), EmailStatus.ENABLED
        passwordPolicy.setResetTokenTtlHours(100)
                .setResetEmailStatus(EmailStatus.DISABLED)
                .setResetSuccessEmailStatus(EmailStatus.DISABLED)
        passwordPolicy.save()

        //Let's check that the new state is properly retrieved in a new instance
        def retrievedPasswordPolicy = client.getResource(passwordPolicy.href, PasswordPolicy.class)
        assertEquals retrievedPasswordPolicy.getResetTokenTtlHours(), 100
        assertEquals retrievedPasswordPolicy.getResetEmailStatus(), EmailStatus.DISABLED
        assertEquals retrievedPasswordPolicy.getResetSuccessEmailStatus(), EmailStatus.DISABLED
    }

    /**
     * @since 1.0.RC4
     */
    @Test
    void testGetAccountsWithTimestampFilter() {
        Directory directory = client.instantiate(Directory)
        directory.name = uniquify("Java SDK: TenantIT.testGetDirectoriesWithTimestampFilters")
        directory = client.createDirectory(directory);
        deleteOnTeardown(directory)

        Account account = client.instantiate(Account)
        account = account.setGivenName('John')
                .setSurname('Test')
                .setEmail('emailme@test.com')
                .setPassword('Changeme123')

        directory.createAccount(account)


        DateFormat dateFormatter = new ISO8601DateFormat();
        String creationTimestamp = dateFormatter.format(account.createdAt)

        def accountList = directory.getAccounts(Accounts.where(Accounts.createdAt().matches(creationTimestamp)))

        assertNotNull accountList
        def retrieved = accountList.iterator().next()
        assertEquals retrieved.href, account.href
        assertEquals dateFormatter.format(retrieved.createdAt), creationTimestamp
    }

    /**
     * @since 1.0.RC4
     */
    @Test
    void testGetDirectoriesWithDateCriteria() {

        Directory directory = client.instantiate(Directory)
        directory.name = uniquify("Java SDK: DirectoryIT.testGetDirectoriesWithDateCriteria")
        directory = client.createDirectory(directory);
        deleteOnTeardown(directory)

        Date dirCreationTimestamp = directory.createdAt
        DateFormat df = new ISO8601DateFormat()

        //matches
        def creationTimestamp = df.format(directory.getCreatedAt());
        def dirList = client.getDirectories(Directories.where(Directories.createdAt().matches(creationTimestamp)))
        assertNotNull dirList.href

        def retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //equals
        dirList = client.getDirectories(Directories.where(Directories.createdAt().equals(dirCreationTimestamp)))
        assertNotNull dirList.href

        retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //gt
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase("Java SDK: DirectoryIT.testGetDirectoriesWithDateCriteria"))
                .and(Directories.createdAt().gt(dirCreationTimestamp)))
        assertNotNull dirList.href
        assertFalse dirList.iterator().hasNext()

        //gte
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().gte(dirCreationTimestamp)))
        assertNotNull dirList.href
        assertTrue dirList.iterator().hasNext()
        retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals retrieved.name, directory.name
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //lt
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().lt(dirCreationTimestamp)))
        assertNotNull dirList.href
        assertFalse dirList.iterator().hasNext()

        //lte
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().lte(dirCreationTimestamp)))
        assertNotNull dirList.href
        assertTrue dirList.iterator().hasNext()
        retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals retrieved.name, directory.name
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //in
        Calendar cal = Calendar.getInstance()
        cal.setTime(dirCreationTimestamp)
        cal.add(Calendar.SECOND, 2)
        Date afterCreationDate = cal.getTime()

        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().in(dirCreationTimestamp, afterCreationDate)))
        assertNotNull dirList.href
        assertTrue dirList.iterator().hasNext()
        retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals retrieved.name, directory.name
        assertEquals df.format(retrieved.createdAt), creationTimestamp

        //in
        cal.setTime(dirCreationTimestamp)
        cal.add(Calendar.SECOND, -10)
        Date newDate = cal.getTime()
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().in(newDate, new Duration(1, TimeUnit.SECONDS))))
        assertNotNull dirList.href
        assertFalse dirList.iterator().hasNext()

        //in
        dirList = client.getDirectories(Directories.where(Directories.name().eqIgnoreCase(directory.name))
                .and(Directories.createdAt().in(dirCreationTimestamp, new Duration(1, TimeUnit.MINUTES))))
        assertNotNull dirList.href
        assertTrue dirList.iterator().hasNext()
        retrieved = dirList.iterator().next()
        assertEquals retrieved.href, directory.href
        assertEquals retrieved.name, directory.name
        assertEquals df.format(retrieved.createdAt), creationTimestamp
    }
}
