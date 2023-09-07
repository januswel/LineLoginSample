import React from 'react';
import {
  Button,
  Image,
  SafeAreaView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {login, logout, Profile} from './src/LineLogin';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

function App(): JSX.Element {
  const [profile, setProfile] = React.useState<Profile | null>(null);
  const handleTapLogin = React.useCallback(async () => {
    console.log('login');
    try {
      const result = await login('hoge');
      setProfile(result);
    } catch (e) {
      console.log(e);
    }
  }, []);
  const handleTapLogout = React.useCallback(async () => {
    console.log('logout');
    await logout();
    setProfile(null);
  }, []);
  const isLoggedIn = profile !== null;

  return (
    <SafeAreaView style={styles.container}>
      {isLoggedIn ? (
        <>
          <View>
            <Text>{profile.displayName}</Text>
            <Text>{profile.email}</Text>
            <Image source={{uri: profile.pictureUrl, width: 64, height: 64}} />
          </View>
          <Button title="logout" onPress={handleTapLogout} />
        </>
      ) : (
        <Button title="login" onPress={handleTapLogin} />
      )}
    </SafeAreaView>
  );
}

export default App;
