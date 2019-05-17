package com.android.passmanager;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;


import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public ActivityTestRule <Login> mActivityRule = new ActivityTestRule<>(
            Login.class);
    @Test
    public void useAppContext() {

        onView( withId( R.id.userinput ) ).perform(replaceText( "SM-G9350" ),closeSoftKeyboard());
        onView( withId( R.id.passinput ) ).perform( replaceText( "1521" ),closeSoftKeyboard());
        onView( withId( R.id.fingerView) ).perform( click() );
        //onView( withId( R.id.add_view ) ).perform( click() );
        for(int i =0;i<100;i++){
            onView( withId( R.id.add_view ) ).perform( click() );
            onView( withId( R.id.title ) ).perform( replaceText( "示例标题"+i ) );
            onView( withId( R.id.account ) ).perform( replaceText("示例账号"+i  ) );
            onView( withId( R.id.password ) ).perform( replaceText( "00000"+i ) );
            onView( withId( R.id.reply ) ).perform( replaceText( "00000"+i ) );
            //onView( withId( R.id.secPass ) ).perform( replaceText( "123" ) );
            //add_new_message
            onView( withId( R.id.add_new_message) ).perform( click() );
        }

        try {
            Thread.sleep( 50000 );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
