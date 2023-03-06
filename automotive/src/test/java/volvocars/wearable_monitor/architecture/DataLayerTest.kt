package volvocars.wearable_monitor.architecture

import androidx.room.RoomDatabase
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.junit.ArchUnitRunner
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.runner.RunWith

@RunWith(ArchUnitRunner::class)
@AnalyzeClasses(packages = ["com.volvocars.wearable_monitor"])
internal class DataLayerTest {

    @ArchTest
    val `DAOs must reside in a dao package` =
        classes().that().haveSimpleNameEndingWith("Dao")
            .should().resideInAPackage("..dao..")

//    @ArchTest
//    val `DAOS must be annotated with Dao` =
//        classes().that().haveSimpleNameEndingWith("Dao").and()
//            .areTopLevelClasses().should().beAnnotatedWith("Dao")

    @ArchTest
    val `DTOs must reside in a dto package` = classes().that().haveSimpleNameEndingWith("Dto")
        .should().resideInAPackage("..dto..")

    @ArchTest
    val `Entities must reside in a entity package` =
        classes().that().haveSimpleNameEndingWith("Entity")
            .should().resideInAPackage("..entity..")

    @ArchTest
    val `Databases must reside in database package` =
        classes().that().haveSimpleNameEndingWith("Database")
            .should().resideInAPackage("..database..")

    @ArchTest
    val `view model subclasses should have correct name` =
        classes().that().areAssignableTo(RoomDatabase::class.java)
            .should().haveSimpleNameContaining("Database")

    @ArchTest
    val `classes named ViewModel should have correct super class` =
        classes().that().haveSimpleNameEndingWith("Database")
            .should().beAssignableTo(RoomDatabase::class.java)


}