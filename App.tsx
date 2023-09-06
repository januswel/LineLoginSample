import React from 'react';
import {Button, SafeAreaView, StyleSheet} from 'react-native';

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

function App(): JSX.Element {
  const handleTapLogin = React.useCallback(() => {
    console.log('login');
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <Button title="login" onPress={handleTapLogin} />
    </SafeAreaView>
  );
}

export default App;
